package ir.erfansn.artouch.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ir.erfansn.artouch.R
import ir.erfansn.artouch.databinding.FragmentCameraBinding
import ir.erfansn.artouch.fragment.PermissionsFragment.Companion.isCameraPermissionGranted

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

    override fun onResume() {
        super.onResume()
        if (!isCameraPermissionGranted) {
            findNavController().navigate(R.id.action_cameraFragment_to_permissionsFragment)
        }
    }
}
