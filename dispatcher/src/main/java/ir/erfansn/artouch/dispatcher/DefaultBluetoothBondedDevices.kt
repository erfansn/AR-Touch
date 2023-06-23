package ir.erfansn.artouch.dispatcher

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class DefaultBluetoothBondedDevices(
    bluetoothAdapter: BluetoothAdapter,
    coroutineScope: CoroutineScope,
) : BluetoothBondedDevices {

    @SuppressLint("MissingPermission")
    override val devices = flow {
        while (true) {
            emit(runCatching { bluetoothAdapter.bondedDevices }.getOrDefault(emptySet()).toList())
            delay(1000)
        }
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )
}
