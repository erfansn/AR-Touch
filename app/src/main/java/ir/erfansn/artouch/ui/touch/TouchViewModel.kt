package ir.erfansn.artouch.ui.touch

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.graphics.PointF
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ir.erfansn.artouch.dispatcher.ble.peripheral.device.DefaultArTouchPeripheralDevice
import ir.erfansn.artouch.producer.DefaultTouchEventProducer
import ir.erfansn.artouch.producer.detector.hand.MediaPipeHandDetector
import ir.erfansn.artouch.producer.detector.marker.ArUcoMarkerDetector
import ir.erfansn.artouch.ui.touch.TouchFragment.Companion.CENTRAL_DEVICE_KEY
import ir.erfansn.artouch.ui.touch.TouchFragment.Companion.DEBUG_MODE_KEY
import kotlinx.coroutines.launch

class TouchViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val centralDevice: BluetoothDevice = checkNotNull(savedStateHandle[CENTRAL_DEVICE_KEY])
    val debugMode: Boolean = checkNotNull(savedStateHandle[DEBUG_MODE_KEY])

    private val arTouchPeripheralDevice = DefaultArTouchPeripheralDevice(
        context = application,
        centralDevice = centralDevice,
        scope = viewModelScope,
    )
    val connectionState = arTouchPeripheralDevice.connectionState

    private val handDetector = MediaPipeHandDetector(
        context = application,
        coroutineScope = viewModelScope,
    )
    val handDetectionResult = handDetector.result

    private val markerDetector = ArUcoMarkerDetector()
    val markerDetectionResult = markerDetector.result

    private val touchEventProducer = DefaultTouchEventProducer(
        handDetector = handDetector,
        markerDetector = markerDetector,
    )
    val touchEvent = touchEventProducer.touchEvent

    fun makeLifecycleAware(addObserver: (LifecycleObserver) -> Unit) {
        addObserver(arTouchPeripheralDevice)
    }

    fun reconnectSelectedDevice() {
        arTouchPeripheralDevice.disconnect()
        arTouchPeripheralDevice.connect()
    }

    fun detectHand(imageProxy: ImageProxy) {
        handDetector.detect(imageProxy)
    }

    fun detectMarker(imageProxy: ImageProxy) {
        markerDetector.detect(imageProxy)
    }

    fun dispatchTouch(tapped: Boolean, point: PointF) {
        viewModelScope.launch {
            arTouchPeripheralDevice.dispatchTouch(tapped, point)
        }
    }
}
