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
