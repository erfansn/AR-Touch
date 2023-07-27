/*
 * Copyright 2023 Erfan Sn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
