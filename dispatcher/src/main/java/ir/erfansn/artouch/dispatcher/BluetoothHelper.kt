package ir.erfansn.artouch.dispatcher

import kotlinx.coroutines.flow.Flow

interface BluetoothHelper {
    val enabled: Boolean
    val allPermissionsGranted: Boolean
    val bondedDevices: Flow<List<BondedDevice>>
}
