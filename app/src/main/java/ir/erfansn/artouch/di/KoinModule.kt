package ir.erfansn.artouch.di

import ir.erfansn.artouch.dispatcher.BluetoothHelper
import ir.erfansn.artouch.dispatcher.DefaultBluetoothHelper
import ir.erfansn.artouch.dispatcher.ble.peripheral.advertiser.ArTouchPeripheralAdvertiser
import ir.erfansn.artouch.dispatcher.ble.peripheral.advertiser.BleHidPeripheralAdvertiser
import ir.erfansn.artouch.dispatcher.ble.peripheral.device.ArTouchPeripheralDevice
import ir.erfansn.artouch.dispatcher.ble.peripheral.device.DefaultArTouchPeripheralDevice
import ir.erfansn.artouch.producer.detector.ObjectDetector
import ir.erfansn.artouch.producer.detector.hand.HandDetectionResult
import ir.erfansn.artouch.producer.detector.hand.MediaPipeHandDetector
import ir.erfansn.artouch.producer.detector.marker.ArUcoMarkerDetector
import ir.erfansn.artouch.producer.detector.marker.MarkersDetectionResult
import ir.erfansn.artouch.producer.extractor.DefaultTouchPositionExtractor
import ir.erfansn.artouch.producer.extractor.TouchPositionExtractor
import ir.erfansn.artouch.ui.configuration.ConfigurationViewModel
import ir.erfansn.artouch.ui.touch.TouchViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.named
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { CoroutineScope(Dispatchers.Default) }

    factoryOf(::DefaultBluetoothHelper) bind BluetoothHelper::class
    factoryOf(::ArTouchPeripheralAdvertiser) bind BleHidPeripheralAdvertiser::class
    viewModelOf(::ConfigurationViewModel)

    factoryOf(::DefaultArTouchPeripheralDevice) bind ArTouchPeripheralDevice::class
    factoryOf(::MediaPipeHandDetector) {
        named("hand_detector")
        bind<ObjectDetector<HandDetectionResult>>()
    }
    factoryOf(::ArUcoMarkerDetector) {
        named("marker_detector")
        bind<ObjectDetector<MarkersDetectionResult>>()
    }
    factoryOf(::DefaultTouchPositionExtractor) bind TouchPositionExtractor::class
    viewModel {
        TouchViewModel(
            get(),
            get(),
            get(named("hand_detector")),
            get(named("marker_detector")),
        )
    }
}