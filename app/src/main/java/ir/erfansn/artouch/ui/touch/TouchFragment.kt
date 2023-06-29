package ir.erfansn.artouch.ui.touch

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.DisplayOrientedMeteringPointFactory
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.concurrent.futures.await
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import ir.erfansn.artouch.R
import ir.erfansn.artouch.databinding.FragmentTouchBinding
import ir.erfansn.artouch.dispatcher.ble.peripheral.BleHidConnectionState
import ir.erfansn.artouch.enableImmersiveMode
import ir.erfansn.artouch.ui.configuration.ConfigurationFragment.Companion.allPermissionsGranted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class TouchFragment : Fragment() {

    private var _binding: FragmentTouchBinding? = null
    private val binding get() = _binding!!
    private val backgroundExecutor = Executors.newFixedThreadPool(2)
    private val viewModel by viewModels<TouchViewModel>()

    private var preview: Preview? = null
    private var handAnalysis: ImageAnalysis? = null
    private var markerAnalysis: ImageAnalysis? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Camera permission is checked here because when the user revokes this permission,
        // the system destroys the app process but it saves the state program (Navigation back stack),
        // with moving check logic to here we prevent unexpected bugs
        if (!allPermissionsGranted) {
            findNavController().popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTouchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.makeLifecycleAware(viewLifecycleOwner.lifecycle::addObserver)

        binding.reconnect.setOnClickListener {
            viewModel.reconnectSelectedDevice()
        }
        binding.utilityButton.setOnClickListener {
            findNavController().popBackStack()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (viewModel.debugMode) {
                    launch {
                        viewModel.handDetectionResult
                            .collect {
                                binding.handLandmarks.result = it
                                Log.d(TAG, "Hand detection time inference: ${it.inferenceTime}")
                            }
                    }
                    launch {
                        viewModel.markerDetectionResult
                            .collect {
                                binding.markersPosition.result = it
                                Log.d(TAG, "ArUco detection time inference: ${it.inferenceTime}")
                            }
                    }
                }

                launch {
                    viewModel.connectionState
                        .onEachIf(viewModel.debugMode) {
                            Log.d(TAG, "ArTouchConnectionState changed to $it")
                        }
                        .collect {
                            if (it == BleHidConnectionState.Connected) {
                                startCamera()
                            } else {
                                closeCamera()
                            }
                            when (it) {
                                BleHidConnectionState.Connected -> {
                                    binding.connectionState.isVisible = false
                                }
                                BleHidConnectionState.Disconnected -> with(binding) {
                                    connectionState.isVisible = true
                                    connectingIndicator.isVisible = false
                                    userMessage.text = getString(R.string.device_is_disconnected)
                                    reconnect.isVisible = true
                                    utilityButton.isVisible = true
                                    utilityButton.text = getString(R.string.select_another_device)
                                }
                                BleHidConnectionState.Connecting -> with(binding) {
                                    connectionState.isVisible = true
                                    connectingIndicator.isVisible = true
                                    userMessage.text = getString(R.string.connecting)
                                    reconnect.isVisible = false
                                    utilityButton.isVisible = true
                                    utilityButton.text = getString(R.string.cancel)
                                }
                                BleHidConnectionState.FailedToConnect -> with(binding) {
                                    connectionState.isVisible = true
                                    connectingIndicator.isVisible = false
                                    userMessage.text = getString(R.string.error_when_connecting)
                                    utilityButton.isVisible = true
                                    utilityButton.text = getString(R.string.select_another_device)
                                }
                            }
                        }
                }
                launch {
                    viewModel.touchEvent
                        .onEachIf(viewModel.debugMode) { (pressed, position) ->
                            binding.touchEvent.text = getString(
                                R.string.touch_event,
                                position.x,
                                position.y,
                                if (pressed) getString(R.string.pressed) else getString(R.string.released),
                            )
                            Log.d(TAG, "Touch event: position=$position, pressed=$pressed")
                        }
                        .collect { (tapped, point) ->
                            viewModel.dispatchTouch(
                                tapped = tapped,
                                point = point,
                            )
                        }
                }
            }
        }

        val touchEventTopMargin = binding.touchEvent.marginTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.touchEvent) { touchPosition, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.statusBars())

            touchPosition.updateLayoutParams<MarginLayoutParams> { topMargin = touchEventTopMargin + insets.top }
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onStart() {
        super.onStart()
        requireActivity().window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            enableImmersiveMode()
        }
    }

    private suspend fun startCamera() {
        val cameraProvider = ProcessCameraProvider.getInstance(requireContext()).await()
        cameraProvider.rebindUseCases()
    }

    private suspend fun closeCamera() {
        val cameraProvider = ProcessCameraProvider.getInstance(requireContext()).await()
        cameraProvider.unbindAll()
    }

    private fun ProcessCameraProvider.rebindUseCases() {
        preview = Preview.Builder()
            .build()
        handAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
        markerAnalysis = ImageAnalysis.Builder()
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .setResolutionSelector(ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                .build())
            .build()

        unbindAll()
        val camera = bindToLifecycle(
            viewLifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            handAnalysis,
            markerAnalysis
        )

        camera.setupFocusController()
        preview?.setSurfaceProvider(binding.preview.surfaceProvider)
        handAnalysis?.setAnalyzer(backgroundExecutor, viewModel::detectHand)
        markerAnalysis?.setAnalyzer(backgroundExecutor, viewModel::detectMarker)
    }

    private fun Camera.setupFocusController() {
        val gestureDetector = GestureDetectorCompat(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            private var isManuallyFocusEnable = false

            @SuppressLint("ShowToast")
            override fun onLongPress(event: MotionEvent) {
                if (isManuallyFocusEnable) {
                    cameraControl.cancelFocusAndMetering()
                    Snackbar.make(binding.root, getString(R.string.auto_focus_mode_enabled), LENGTH_SHORT).showSafely()
                    isManuallyFocusEnable = false
                }
            }

            @SuppressLint("ShowToast")
            override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
                val meteringPoint = DisplayOrientedMeteringPointFactory(
                    binding.preview.display,
                    cameraInfo,
                    binding.preview.width.toFloat(),
                    binding.preview.height.toFloat(),
                )
                val focusMeteringAction =
                    FocusMeteringAction.Builder(meteringPoint.createPoint(event.x, event.y))
                        .disableAutoCancel()
                        .build()
                cameraControl.startFocusAndMetering(focusMeteringAction)
                if (!isManuallyFocusEnable) {
                    Snackbar.make(binding.root, getString(R.string.enable_manually_focus_mode), LENGTH_LONG).showSafely()
                    isManuallyFocusEnable = true
                }
                return true
            }
        })
        binding.preview.setOnTouchListener @SuppressLint("ClickableViewAccessibility") { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun Snackbar.showSafely() {
        val snackbarLeftMargin = view.marginLeft
        val snackbarRightMargin = view.marginRight
        val snackbarBottomMargin = view.marginBottom
        ViewCompat.setOnApplyWindowInsetsListener(view) { snackbar, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures())

            snackbar.updateLayoutParams<MarginLayoutParams> {
                bottomMargin = snackbarBottomMargin + insets.bottom
                leftMargin = snackbarLeftMargin + insets.left
                rightMargin = snackbarRightMargin + insets.right
            }
            WindowInsetsCompat.CONSUMED
        }
        show()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        preview?.targetRotation = binding.preview.display.rotation
        handAnalysis?.targetRotation = binding.preview.display.rotation
        markerAnalysis?.targetRotation = binding.preview.display.rotation
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        backgroundExecutor.shutdown()
    }

    companion object {
        private const val TAG = "TouchFragment"

        const val CENTRAL_DEVICE_KEY = "central_device"
        const val DEBUG_MODE_KEY = "debug_mode"
    }
}

fun <T> Flow<T>.onEachIf(predicate: Boolean, action: (T) -> Unit): Flow<T> =
    if (predicate) onEach(action) else this
