package ir.erfansn.artouch.dispatcher.ble.peripheral.device

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import ir.erfansn.artouch.common.util.Point
import ir.erfansn.artouch.dispatcher.BondedDevice
import ir.erfansn.artouch.dispatcher.ble.peripheral.BleHidConnectionState
import kotlinx.coroutines.flow.StateFlow

interface ArTouchPeripheralDevice : DefaultLifecycleObserver {
    var centralDevice: BondedDevice
    val connectionState: StateFlow<BleHidConnectionState>
    fun connect()
    fun disconnect()
    fun dispatchTouch(tapped: Boolean, point: Point)
    fun close()
    override fun onStart(owner: LifecycleOwner) = connect()
    override fun onStop(owner: LifecycleOwner) = disconnect()
}
