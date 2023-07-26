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
