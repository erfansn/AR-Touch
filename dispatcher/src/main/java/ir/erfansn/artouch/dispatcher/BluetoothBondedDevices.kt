package ir.erfansn.artouch.dispatcher

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.Flow

interface BluetoothBondedDevices {
    val devices: Flow<Set<BluetoothDevice>>
}
