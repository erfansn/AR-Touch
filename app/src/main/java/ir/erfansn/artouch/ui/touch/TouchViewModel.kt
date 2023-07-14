package ir.erfansn.artouch.ui.touch

import androidx.camera.core.ImageProxy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import ir.erfansn.artouch.dispatcher.ble.peripheral.device.ArTouchPeripheralDevice
import ir.erfansn.artouch.producer.DefaultTouchEventProducer
import ir.erfansn.artouch.producer.detector.ObjectDetector
import ir.erfansn.artouch.producer.detector.hand.HandDetectionResult
import ir.erfansn.artouch.producer.detector.marker.MarkersDetectionResult
import ir.erfansn.artouch.producer.extractor.TouchPositionExtractor
import ir.erfansn.artouch.common.util.Point
import ir.erfansn.artouch.ui.touch.TouchFragment.Companion.CENTRAL_DEVICE_KEY
import kotlinx.coroutines.CoroutineDispatcher

class TouchViewModel(
    savedStateHandle: SavedStateHandle,
    defaultDispatcher: CoroutineDispatcher,
    touchPositionExtractor: TouchPositionExtractor,
    private val arTouchPeripheralDevice: ArTouchPeripheralDevice,
    private val handDetector: ObjectDetector<HandDetectionResult>,
    private val markerDetector: ObjectDetector<MarkersDetectionResult>,
) : ViewModel() {

    init {
        arTouchPeripheralDevice.centralDevice = checkNotNull(savedStateHandle[CENTRAL_DEVICE_KEY])
    }

    val connectionState = arTouchPeripheralDevice.connectionState

    val handDetectionResult = handDetector.result

    val markerDetectionResult = markerDetector.result

    private val touchEventProducer = DefaultTouchEventProducer(
        handDetectionResult = handDetectionResult,
        markersDetectionResult = markerDetectionResult,
        touchPositionExtractor = touchPositionExtractor,
        defaultDispatcher = defaultDispatcher,
    )
    val touchEvent = touchEventProducer.touchEvent

    fun startConnectingToDevice(lifecycle: Lifecycle) {
        lifecycle.addObserver(arTouchPeripheralDevice)
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

    fun dispatchTouch(tapped: Boolean, point: Point) {
        arTouchPeripheralDevice.dispatchTouch(tapped, point)
    }

    override fun onCleared() {
        super.onCleared()
        arTouchPeripheralDevice.close()
    }
}
