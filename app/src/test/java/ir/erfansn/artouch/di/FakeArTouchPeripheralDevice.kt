package ir.erfansn.artouch.di

import ir.erfansn.artouch.common.util.Point
import ir.erfansn.artouch.dispatcher.BondedDevice
import ir.erfansn.artouch.dispatcher.ble.peripheral.BleHidConnectionState
import ir.erfansn.artouch.dispatcher.ble.peripheral.device.ArTouchPeripheralDevice
import kotlinx.coroutines.flow.MutableStateFlow

class FakeArTouchPeripheralDevice : ArTouchPeripheralDevice {

    override lateinit var centralDevice: BondedDevice

    override val connectionState = MutableStateFlow(BleHidConnectionState.Disconnected)

    override fun connect() = Unit

    override fun disconnect() = Unit

    override fun dispatchTouch(tapped: Boolean, point: Point) = Unit

    override fun close() = Unit
}
