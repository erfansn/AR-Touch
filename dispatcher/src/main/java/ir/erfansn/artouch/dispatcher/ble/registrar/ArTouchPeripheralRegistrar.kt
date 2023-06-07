package ir.erfansn.artouch.dispatcher.ble.registrar

internal interface ArTouchPeripheralRegistrar {
    fun registerDevice()
    fun unregisterDevice()
}
