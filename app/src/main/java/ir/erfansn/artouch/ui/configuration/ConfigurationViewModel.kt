package ir.erfansn.artouch.ui.configuration

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ir.erfansn.artouch.dispatcher.DefaultBluetoothBondedDevices
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

class ConfigurationViewModel(application: Application) : AndroidViewModel(application) {

    private val bluetoothManager = application.getSystemService<BluetoothManager>()!!
    private val bluetoothAdapter = bluetoothManager.adapter
    private val isBluetoothEnable get() = bluetoothAdapter.isEnabled

    private val bluetoothPermissionsGranted
        get() = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            true
        } else {
            ConfigurationFragment.BLUETOOTH_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(
                    getApplication(),
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

    private val bluetoothBondedDevices = DefaultBluetoothBondedDevices(
        bluetoothAdapter,
        viewModelScope,
    )

    val bondedDevices = bluetoothBondedDevices.devices
    var uiState: ConfigurationUiState by mutableStateOf(ConfigurationUiState.DisableBluetooth)
        private set

    init {
        viewModelScope.launch {
            while (isActive) {
                uiState = when {
                    bluetoothPermissionsGranted && isBluetoothEnable -> {
                        ConfigurationUiState.AdvertisingMode
                    }

                    !bluetoothPermissionsGranted && isBluetoothEnable -> {
                        ConfigurationUiState.EnableBluetooth
                    }

                    else -> {
                        ConfigurationUiState.DisableBluetooth
                    }
                }
                yield()
            }
        }
    }

    fun handleBluetoothEnablingResult(isEnabled: Boolean) {
        uiState = if (isEnabled) {
            ConfigurationUiState.AdvertisingMode
        } else {
            ConfigurationUiState.DisableBluetooth
        }
    }

    fun startArTouchAdvertising() {
        uiState = ConfigurationUiState.AdvertisingMode
    }
}

sealed interface ConfigurationUiState {
    object DisableBluetooth : ConfigurationUiState
    object EnableBluetooth : ConfigurationUiState
    object AdvertisingMode : ConfigurationUiState
}
