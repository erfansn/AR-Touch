package ir.erfansn.artouch.dispatcher

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DefaultBluetoothBondedDevices(
    bluetoothAdapter: BluetoothAdapter,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BluetoothBondedDevices {

    @SuppressLint("MissingPermission")
    override val devices = flow<Set<BluetoothDevice>> {
        while (true) {
            emit(bluetoothAdapter.bondedDevices)
            delay(2000)
        }
    }.flowOn(ioDispatcher)
        .distinctUntilChanged()
}

interface BluetoothBondedDevices {
    val devices: Flow<Set<BluetoothDevice>>
}
