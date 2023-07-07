package ir.erfansn.artouch.dispatcher.ble.peripheral.device

import android.bluetooth.BluetoothDevice
import android.graphics.PointF
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import ir.erfansn.artouch.dispatcher.ble.peripheral.BleHidConnectionState
import kotlinx.coroutines.flow.StateFlow

interface ArTouchPeripheralDevice : DefaultLifecycleObserver {
    var centralDevice: BluetoothDevice
    val connectionState: StateFlow<BleHidConnectionState>
    fun connect()
    fun disconnect()
    fun dispatchTouch(tapped: Boolean, point: PointF)
    fun close()
    override fun onStart(owner: LifecycleOwner) = connect()
    override fun onStop(owner: LifecycleOwner) = disconnect()
}
