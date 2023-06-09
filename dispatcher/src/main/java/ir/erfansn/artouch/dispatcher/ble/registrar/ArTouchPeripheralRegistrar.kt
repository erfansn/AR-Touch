package ir.erfansn.artouch.dispatcher.ble.registrar

import android.bluetooth.BluetoothHidDevice
import kotlinx.coroutines.flow.StateFlow

internal interface ArTouchPeripheralRegistrar {
    val connectionState: StateFlow<ArTouchConnectionState>
    suspend fun registerDevice(): BluetoothHidDevice
    fun unregisterDevice()
}
