/*
 * Copyright 2023 Erfan Sn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ir.erfansn.artouch.ui.configuration

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import ir.erfansn.artouch.R
import ir.erfansn.artouch.disableImmersiveMode
import ir.erfansn.artouch.ui.touch.TouchFragment
import org.koin.androidx.compose.koinViewModel

class ConfigurationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            Mdc3Theme {
                val viewModel: ConfigurationViewModel = koinViewModel()
                val boundedDevices by viewModel.bondedDevices.collectAsState()
                val uiState = viewModel.uiState

                ConfigurationScreen(
                    uiState = uiState,
                    bluetoothBondedDevices = boundedDevices,
                    onPromptToEnableBluetooth = {
                        startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    },
                    onStartArTouchAdvertiser = {
                        viewModel.startArTouchAdvertiser(viewLifecycleOwner.lifecycle)
                    },
                    onStopArTouchAdvertiser = {
                        viewModel.stopArTouchAdvertiser(viewLifecycleOwner.lifecycle)
                    },
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

    override fun onStart() {
        super.onStart()
        requireActivity().window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            disableImmersiveMode()
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
