package ir.erfansn.artouch.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.concurrent.futures.await
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import ir.erfansn.artouch.R
import ir.erfansn.artouch.databinding.FragmentCameraBinding
import ir.erfansn.artouch.fragment.PermissionsFragment.Companion.isCameraPermissionGranted
import kotlinx.coroutines.launch

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startCamera()
    }

    private fun startCamera() {
        lifecycleScope.launch {
            val cameraProvider = ProcessCameraProvider.getInstance(requireActivity()).await()

            val preview = Preview.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(requireActivity(), cameraSelector, preview)

            preview.setSurfaceProvider(binding.preview.surfaceProvider)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isCameraPermissionGranted) {
            findNavController().navigate(R.id.action_cameraFragment_to_permissionsFragment)
        }
    }
}
