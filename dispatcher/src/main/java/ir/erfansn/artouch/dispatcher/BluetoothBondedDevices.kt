package ir.erfansn.artouch.dispatcher

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.StateFlow

interface BluetoothBondedDevices {
    val devices: StateFlow<List<BluetoothDevice>>
}
