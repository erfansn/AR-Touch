package ir.erfansn.artouch.dispatcher.ble.registrar

import kotlinx.coroutines.flow.StateFlow

internal interface ArTouchPeripheralRegistrar {
    val connectionState: StateFlow<ArTouchConnectionState>
    fun registerDevice()
    fun unregisterDevice()
}
