package ir.erfansn.artouch.dispatcher.ble.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import kotlinx.coroutines.flow.StateFlow

internal interface BleHidPeripheralRegistrar {
    suspend fun registerDevice(sdpSettings: BluetoothHidDeviceAppSdpSettings)
    fun unregisterDevice()
}

internal interface BleHidPeripheralManager : BleHidPeripheralRegistrar {
    val connectionState: StateFlow<BleHidConnectionState>
    @OptIn(ExperimentalUnsignedTypes::class)
    fun trySendReport(target: BluetoothDevice, id: UByte, data: UByteArray): Boolean
    fun connect(centralDevice: BluetoothDevice)
    fun disconnect(centralDevice: BluetoothDevice)
}

enum class BleHidConnectionState {
    Connected, Disconnected, Connecting, FailedToConnect
}
