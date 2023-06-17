package ir.erfansn.artouch.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.concurrent.futures.await
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import ir.erfansn.artouch.R
import ir.erfansn.artouch.databinding.FragmentCameraBinding
import ir.erfansn.artouch.producer.detector.ObjectDetector
import ir.erfansn.artouch.producer.detector.hand.HandDetectionResult
import ir.erfansn.artouch.producer.detector.hand.MediaPipeHandDetector
import ir.erfansn.artouch.producer.detector.marker.ArUcoMarkerDetector
import ir.erfansn.artouch.producer.detector.marker.MarkerDetectionResult
import ir.erfansn.artouch.producer.DefaultTouchEventProducer
import ir.erfansn.artouch.fragment.PermissionsFragment.Companion.isCameraPermissionGranted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val backgroundExecutor = Executors.newFixedThreadPool(2)

    private lateinit var handDetector: ObjectDetector<HandDetectionResult>
    private lateinit var markerDetector: ObjectDetector<MarkersDetectionResult>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handDetector = MediaPipeHandDetector(
            context = requireContext(),
            coroutineScope = lifecycleScope,
        )
        markerDetector = ArUcoMarkerDetector()

        val touchEventProducer = DefaultTouchEventProducer(
            handDetector = handDetector,
            markerDetector = markerDetector,
        )

        lifecycleScope.launch {
            startCamera()

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (true) {
                    launch {
                        handDetector.result
                            .collect {
                                binding.handLandmarks.result = it
                                Log.d(TAG, "Hand detection time inference: ${it.inferenceTime}")
                            }
                    }
                    launch {
                        markerDetector.result
                            .collect {
                                binding.markersPosition.result = it
                                Log.d(TAG, "ArUco detection time inference: ${it.inferenceTime}")
                            }
                    }
                }

                launch {
                    touchEventProducer.touchEvent
                        .catch {
                            Log.e(TAG, "A error in Touch position extractor is occurred", it)
                        }.collect { (pressed, position) ->
                            if (pressed) {
                                binding.touchPosition.text =
                                    getString(R.string.touch_position, position.x, position.y)
                            }
                            Log.d(TAG, "Touch event is position=$position, pressed=$pressed")
                        }
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.touchPosition) { touchPosition, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures())

            touchPosition.updateLayoutParams<MarginLayoutParams> { topMargin = insets.top }
            WindowInsetsCompat.CONSUMED
        }
    }

    private suspend fun startCamera() {
        val cameraProvider = ProcessCameraProvider.getInstance(requireContext()).await()

        cameraProvider.rebindUseCases()
    }

    private fun ProcessCameraProvider.rebindUseCases() {
        val preview = Preview.Builder()
            .build()
        val handAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
        val markerAnalysis = ImageAnalysis.Builder()
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .setResolutionSelector(ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                .build())
            .build()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        unbindAll()
        bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, handAnalysis, markerAnalysis)

        preview.setSurfaceProvider(binding.preview.surfaceProvider)
        handAnalysis.setAnalyzer(backgroundExecutor, handDetector::detect)
        markerAnalysis.setAnalyzer(backgroundExecutor, markerDetector::detect)
    }

    override fun onResume() {
        super.onResume()
        if (!isCameraPermissionGranted) {
            findNavController().navigate(R.id.action_cameraFragment_to_permissionsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        backgroundExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraFragment"
    }
}
