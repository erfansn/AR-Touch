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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.erfansn.artouch.dispatcher.BluetoothHelper
import ir.erfansn.artouch.dispatcher.ble.peripheral.advertiser.BleHidPeripheralAdvertiser
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

class ConfigurationViewModel(
    private val bluetoothHelper: BluetoothHelper,
    private val bleHidPeripheralAdvertiser: BleHidPeripheralAdvertiser,
) : ViewModel() {

    val bondedDevices = bluetoothHelper.bondedDevices
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
    var uiState: ConfigurationUiState by mutableStateOf(ConfigurationUiState.BluetoothDisable)
        private set

    init {
        viewModelScope.launch {
            while (true) {
                uiState = when {
                    bluetoothHelper.allPermissionsGranted && bluetoothHelper.enabled -> {
                        ConfigurationUiState.AdvertisingMode
                    }

                    !bluetoothHelper.allPermissionsGranted && bluetoothHelper.enabled -> {
                        ConfigurationUiState.BluetoothEnable
                    }

                    else -> {
                        ConfigurationUiState.BluetoothDisable
                    }
                }
                yield()
            }
        }
    }

    fun startArTouchAdvertiser(lifecycle: Lifecycle) {
        lifecycle.addObserver(bleHidPeripheralAdvertiser)
    }

    fun stopArTouchAdvertiser(lifecycle: Lifecycle) {
        bleHidPeripheralAdvertiser.stopAdvertising()
        lifecycle.removeObserver(bleHidPeripheralAdvertiser)
    }

    override fun onCleared() {
        super.onCleared()
        bleHidPeripheralAdvertiser.close()
    }
}

sealed interface ConfigurationUiState {
    object BluetoothDisable : ConfigurationUiState
    object BluetoothEnable : ConfigurationUiState
    object AdvertisingMode : ConfigurationUiState
}
