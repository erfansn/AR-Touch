package ir.erfansn.artouch.dispatcher.ble.peripheral.device

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.content.Context
import android.util.Log
import ir.erfansn.artouch.common.util.Point
import ir.erfansn.artouch.dispatcher.ble.ArTouchSpecification
import ir.erfansn.artouch.dispatcher.ble.peripheral.DefaultBleHidPeripheralManager
import ir.erfansn.artouch.dispatcher.ble.peripheral.BleHidConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal class DefaultArTouchPeripheralDevice(context: Context) : ArTouchPeripheralDevice {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val bleHidPeripheralManager = DefaultBleHidPeripheralManager(context)
    override val connectionState = bleHidPeripheralManager.connectionState

    override lateinit var centralDevice: BluetoothDevice

    override fun connect() {
        check(::centralDevice.isInitialized) { "Must set a central device" }

        coroutineScope.launch {
            bleHidPeripheralManager.registerDevice(
                sdpSettings = BluetoothHidDeviceAppSdpSettings(
                    ArTouchSpecification.NAME,
                    ArTouchSpecification.DESCRIPTION,
                    ArTouchSpecification.PROVIDER,
                    ArTouchSpecification.SUBCLASS,
                    ArTouchSpecification.REPORT_DESCRIPTOR
                )
            )
            bleHidPeripheralManager.connect(centralDevice)
        }
    }

    override fun disconnect() {
        if (!::centralDevice.isInitialized) return

        bleHidPeripheralManager.disconnect(centralDevice)
        bleHidPeripheralManager.unregisterDevice()
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun dispatchTouch(tapped: Boolean, point: Point) {
        check(connectionState.value == BleHidConnectionState.Connected)
        require(point.x in 0f..1f && point.y in 0f..1f)

        val (x, y) = point * 100_00f
        val (lx, mx) = x.roundToInt().latestAndMostSignificantByte()
        val (ly, my) = y.roundToInt().latestAndMostSignificantByte()

        bleHidPeripheralManager.trySendReport(
            target = centralDevice,
            id = ArTouchSpecification.REPORT_ID,
            data = ubyteArrayOf(
                if (tapped) 0x01u else 0x00u,
                0x00u,
                if (tapped) 0x11u else 0x00u,
                lx,
                mx,
                ly,
                my,
            )
        ).also {
            val reportState = if (it) "was successful" else "occurred a failure"
            Log.i(TAG, "Sending the report $reportState")
        }
    }

    private fun Int.latestAndMostSignificantByte() =
        (this and 0xFF).toUByte() to (this shr 8 and 0xFF).toUByte()

    override fun close() {
        coroutineScope.cancel()
    }

    companion object {
        private const val TAG = "DefaultArTouchPeripheralDevice"
    }
}
