package ir.erfansn.artouch.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import ir.erfansn.artouch.R
import ir.erfansn.artouch.databinding.FragmentPermissionsBinding
import ir.erfansn.artouch.get
import ir.erfansn.artouch.set

class PermissionsFragment : Fragment() {

    private var _binding: FragmentPermissionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPermissionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isCameraPermissionGranted) {
            // TODO: Navigate to CameraFragment
        }
    }

    override fun onResume() {
        super.onResume()
        when {
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.CAMERA
            ) -> {
                prepareUserMessage(
                    message = getString(R.string.camera_permission_required),
                    actionLabel = getString(R.string.ok),
                    action = {
                        shouldShowGoToAppSettings[CAMERA_PERMISSION] = true
                        requestCameraPermission.launch(CAMERA_PERMISSION)
                    }
                )
            }

            !isCameraPermissionGranted && shouldShowGoToAppSettings[CAMERA_PERMISSION] -> {
                prepareUserMessage(
                    message = getString(R.string.granting_camera_permission_from_settings),
                    actionLabel = getString(R.string.go),
                    action = {
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(Uri.fromParts("package", requireContext().packageName, null))
                            .also(::startActivity)
                    }
                )
            }

            else -> {
                requestCameraPermission.launch(CAMERA_PERMISSION)
            }
        }
    }

    private fun prepareUserMessage(
        message: String,
        actionLabel: String,
        action: () -> Unit,
    ) {
        binding.messageGroup.isVisible = true
        binding.userMessage.text = message
        binding.actionButton.apply {
            text = actionLabel
            setOnClickListener { action() }
        }
    }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // TODO: Navigate to CameraFragment
                Log.i(TAG, "Camera permission granted")
            } else {
                Log.i(TAG, "Camera permission denied")
            }
        }

    private val shouldShowGoToAppSettings: SharedPreferences
        get() = requireContext().getSharedPreferences(
            getString(R.string.preference_permissions_rationale_shown),
            Context.MODE_PRIVATE
        )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "PermissionsFragment"

        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA

        val Fragment.isCameraPermissionGranted
            get() = ContextCompat.checkSelfPermission(
                requireContext(),
                CAMERA_PERMISSION,
            ) == PackageManager.PERMISSION_GRANTED
    }
}
