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
