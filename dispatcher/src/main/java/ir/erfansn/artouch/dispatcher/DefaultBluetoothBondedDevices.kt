package ir.erfansn.artouch.dispatcher

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DefaultBluetoothBondedDevices(
    context: Context,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BluetoothBondedDevices {

    private val bluetoothManager = context.getSystemService<BluetoothManager>()!!
    private val bluetoothAdapter = bluetoothManager.adapter

    @SuppressLint("MissingPermission")
    override val devices = flow<Set<BluetoothDevice>> {
        while (true) {
            emit(bluetoothAdapter.bondedDevices)
            delay(2000)
        }
    }.flowOn(ioDispatcher)
        .distinctUntilChanged()
}
