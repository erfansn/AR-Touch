package ir.erfansn.artouch.dispatcher.ble.peripheral.device

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.graphics.PointF
import android.util.Log
import androidx.core.graphics.component1
import androidx.core.graphics.component2
import androidx.core.graphics.times
import androidx.core.graphics.toPoint
import ir.erfansn.artouch.dispatcher.ble.ArTouchSpecification
import ir.erfansn.artouch.dispatcher.ble.peripheral.ArTouchPeripheralManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("MissingPermission")
class DefaultArTouchPeripheralDevice(
    context: Context,
    private val centralDevice: BluetoothDevice,
    private val scope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ArTouchPeripheralDevice {

    private val arTouchPeripheralManager = ArTouchPeripheralManager(
        context = context,
        scope = scope,
    )

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun dispatchTouch(tapped: Boolean, point: PointF) {
        withContext(ioDispatcher) {
            require(point.x in 0f..1f && point.y in 0f..1f)
            val (x, y) = (point * 100_00f).toPoint()
            val (lx, mx) = x.latestAndMostSignificantByte()
            val (ly, my) = y.latestAndMostSignificantByte()

            arTouchPeripheralManager.trySendReport(
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
    }

    private fun Int.latestAndMostSignificantByte() =
        (this and 0xFF).toUByte() to (this shr 8 and 0xFF).toUByte()

    override val connectionState = arTouchPeripheralManager.connectionState

    override fun connect() {
        scope.launch {
            arTouchPeripheralManager.registerDevice()
            arTouchPeripheralManager.connect(centralDevice)
        }
    }

    override fun disconnect() {
        arTouchPeripheralManager.disconnect(centralDevice)
        arTouchPeripheralManager.unregisterDevice()
    }

    companion object {
        private const val TAG = "DefaultArTouchPeripheralDevice"
    }
}
