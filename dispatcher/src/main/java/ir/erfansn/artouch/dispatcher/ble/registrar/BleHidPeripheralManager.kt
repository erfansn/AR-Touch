package ir.erfansn.artouch.dispatcher.ble.registrar

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.StateFlow

internal interface BleHidPeripheralRegistrar {
    suspend fun registerDevice()
    fun unregisterDevice()
}

internal interface BleHidPeripheralManager : BleHidPeripheralRegistrar {
    val connectionState: StateFlow<BleHidConnectionState>
    @OptIn(ExperimentalUnsignedTypes::class)
    fun trySendReport(
        target: BluetoothDevice,
        id: UByte,
        data: UByteArray,
    ): Boolean
    fun connect(centralDevice: BluetoothDevice)
    fun disconnect(centralDevice: BluetoothDevice)
}
