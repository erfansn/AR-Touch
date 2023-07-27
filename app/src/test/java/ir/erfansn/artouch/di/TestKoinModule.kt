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

package ir.erfansn.artouch.di

import androidx.lifecycle.SavedStateHandle
import ir.erfansn.artouch.dispatcher.BondedDevice
import ir.erfansn.artouch.dispatcher.ble.peripheral.device.ArTouchPeripheralDevice
import ir.erfansn.artouch.producer.detector.ObjectDetector
import ir.erfansn.artouch.producer.di.ARUCO_DETECTOR_QUALIFIER
import ir.erfansn.artouch.producer.di.HAND_DETECTOR_QUALIFIER
import ir.erfansn.artouch.producer.extractor.TouchPositionExtractor
import ir.erfansn.artouch.ui.touch.TouchFragment
import org.koin.dsl.bind
import org.koin.dsl.module

fun testModule(arTouchPeripheralDevice: ArTouchPeripheralDevice) = module {
    single { SavedStateHandle(mapOf(TouchFragment.CENTRAL_DEVICE_KEY to BondedDevice())) }
    single(HAND_DETECTOR_QUALIFIER) { DummyObjectDetector } bind ObjectDetector::class
    single(ARUCO_DETECTOR_QUALIFIER) { DummyObjectDetector } bind ObjectDetector::class
    factory { arTouchPeripheralDevice } bind ArTouchPeripheralDevice::class
    single { DummyTouchPositionExtractor } bind TouchPositionExtractor::class
}
