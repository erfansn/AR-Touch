/*
 * Copyright (C) 2023 ErfanSn
 *
 * AR Touch is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AR Touch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AR Touch.  If not, see <https://www.gnu.org/licenses/>.
 */

package ir.erfansn.artouch.ui.touch

import androidx.camera.core.ImageProxy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import ir.erfansn.artouch.dispatcher.ble.peripheral.device.ArTouchPeripheralDevice
import ir.erfansn.artouch.producer.DefaultTouchEventProducer
import ir.erfansn.artouch.producer.detector.ObjectDetector
import ir.erfansn.artouch.producer.detector.hand.HandDetectionResult
import ir.erfansn.artouch.producer.detector.aruco.ArUcoDetectionResult
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
    private val arucoDetector: ObjectDetector<ArUcoDetectionResult>,
) : ViewModel() {

    init {
        arTouchPeripheralDevice.centralDevice = checkNotNull(savedStateHandle[CENTRAL_DEVICE_KEY])
    }

    val connectionState = arTouchPeripheralDevice.connectionState

    val handDetectionResult = handDetector.result

    val arucoDetectionResult = arucoDetector.result

    private val touchEventProducer = DefaultTouchEventProducer(
        handDetectionResult = handDetectionResult,
        arUcoDetectionResult = arucoDetectionResult,
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

    fun detectArUco(imageProxy: ImageProxy) {
        arucoDetector.detect(imageProxy)
    }

    fun dispatchTouch(tapped: Boolean, point: Point) {
        arTouchPeripheralDevice.dispatchTouch(tapped, point)
    }

    override fun onCleared() {
        super.onCleared()
        arTouchPeripheralDevice.close()
    }
}
