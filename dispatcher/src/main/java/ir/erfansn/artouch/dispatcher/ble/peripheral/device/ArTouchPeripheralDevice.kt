package ir.erfansn.artouch.dispatcher.ble.peripheral.device

import android.graphics.PointF
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import ir.erfansn.artouch.dispatcher.ble.peripheral.BleHidConnectionState
import kotlinx.coroutines.flow.Flow

interface ArTouchPeripheralDevice : DefaultLifecycleObserver {
    val connectionState: Flow<BleHidConnectionState>
    suspend fun dispatchTouch(tapped: Boolean, point: PointF)
    fun connect()
    fun disconnect()
    override fun onStart(owner: LifecycleOwner) = connect()
    override fun onStop(owner: LifecycleOwner) = disconnect()
}
