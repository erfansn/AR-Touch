package ir.erfansn.artouch.ui.configuration

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import ir.erfansn.artouch.R
import ir.erfansn.artouch.dispatcher.ble.peripheral.advertiser.ArTouchPeripheralAdvertiser
import ir.erfansn.artouch.ui.touch.TouchFragment

class ConfigurationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            Mdc3Theme {
                val viewModel: ConfigurationViewModel = viewModel()
                val boundedDevices by viewModel.bondedDevices.collectAsState()
                val uiState = viewModel.uiState

                val arTouchPeripheralAdvertiser = remember {
                    ArTouchPeripheralAdvertiser(
                        context = requireContext(),
                        scope = viewLifecycleOwner.lifecycleScope,
                    )
                }
                DisposableEffect(uiState) {
                    when (uiState) {
                        ConfigurationUiState.AdvertisingMode -> {
                            viewLifecycleOwner.lifecycle.addObserver(arTouchPeripheralAdvertiser)
                        }

                        ConfigurationUiState.DisableBluetooth, ConfigurationUiState.EnableBluetooth -> {
                            arTouchPeripheralAdvertiser.stopAdvertising()
                        }
                    }
                    onDispose {
                        viewLifecycleOwner.lifecycle.removeObserver(arTouchPeripheralAdvertiser)
                    }
                }

                val bluetoothEnablerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    viewModel.handleBluetoothEnablingResult(
                        isEnabled = it.resultCode == Activity.RESULT_OK
                    )
                }
                ConfigurationScreen(
                    uiState = uiState,
                    bluetoothBondedDevices = boundedDevices,
                    onPromptToEnableBluetooth = {
                        bluetoothEnablerLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    },
                    onStartArTouchAdvertiser = viewModel::startArTouchAdvertising,
                    onNavigateToCameraFragment = { debugMode, centralDevice ->
                        findNavController().navigate(
                            resId = R.id.action_configurationFragment_to_touchFragment,
                            args = bundleOf(
                                TouchFragment.DEBUG_MODE_KEY to debugMode,
                                TouchFragment.CENTRAL_DEVICE_KEY to centralDevice,
                            )
                        )
                    }
                )
            }
        }
    }

    companion object {
        const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        @RequiresApi(Build.VERSION_CODES.S)
        val BLUETOOTH_PERMISSIONS = arrayOf(
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
        )

        private val ALL_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(*BLUETOOTH_PERMISSIONS, CAMERA_PERMISSION)
        } else {
            listOf(CAMERA_PERMISSION)
        }
        val Fragment.allPermissionsGranted
            get() = ALL_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    it,
                ) == PackageManager.PERMISSION_GRANTED
            }
    }
}
