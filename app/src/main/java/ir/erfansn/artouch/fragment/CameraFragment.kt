package ir.erfansn.artouch.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.concurrent.futures.await
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import ir.erfansn.artouch.R
import ir.erfansn.artouch.databinding.FragmentCameraBinding
import ir.erfansn.artouch.fragment.PermissionsFragment.Companion.isCameraPermissionGranted
import ir.erfansn.artouch.handdetection.HandDetector
import ir.erfansn.artouch.handdetection.MediaPipeHandDetector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val backgroundExecutor = Executors.newSingleThreadExecutor()

    private lateinit var handDetector: HandDetector

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

        handDetector = MediaPipeHandDetector(context = requireContext())

        lifecycleScope.launch {
            startCamera()

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                handDetector.result
                    .catch {
                        Log.e(TAG, "A error in Hand detector is occurred", it)
                    }.collect {
                        binding.handLandmarks.setResult(it)
                        Log.d(TAG, "Hand detection time inference: ${it.inferenceTime}")
                    }
            }
        }
    }

    private suspend fun startCamera() {
        val cameraProvider = ProcessCameraProvider.getInstance(requireContext()).await()

        cameraProvider.rebindUseCases()
    }

    private fun ProcessCameraProvider.rebindUseCases() {
        val preview = Preview.Builder().build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        unbindAll()
        bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageAnalysis)

        preview.setSurfaceProvider(binding.preview.surfaceProvider)
        imageAnalysis.setAnalyzer(backgroundExecutor, handDetector::detect)
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
    }

    companion object {
        private const val TAG = "CameraFragment"
    }
}
