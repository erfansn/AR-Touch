package ir.erfansn.artouch.producer.di

import ir.erfansn.artouch.producer.detector.ObjectDetector
import ir.erfansn.artouch.producer.detector.hand.HandDetectionResult
import ir.erfansn.artouch.producer.detector.hand.MediaPipeHandDetector
import ir.erfansn.artouch.producer.detector.marker.ArUcoMarkerDetector
import ir.erfansn.artouch.producer.detector.marker.MarkersDetectionResult
import ir.erfansn.artouch.producer.detector.util.DefaultImageRotationHelper
import ir.erfansn.artouch.producer.detector.util.ImageRotationHelper
import ir.erfansn.artouch.producer.extractor.DefaultTouchPositionExtractor
import ir.erfansn.artouch.producer.extractor.TouchPositionExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.named
import org.koin.core.qualifier.qualifier
import org.koin.dsl.bind
import org.koin.dsl.module

private const val HAND_DETECTOR_NAME = "hand_detector"
private const val MARKER_DETECTOR_NAME = "marker_detector"

val HAND_DETECTOR_QUALIFIER = qualifier(HAND_DETECTOR_NAME)
val MARKER_DETECTOR_QUALIFIER = qualifier(MARKER_DETECTOR_NAME)

val producerModule = module {
    single { CoroutineScope(Dispatchers.Default) }

    factoryOf(::DefaultImageRotationHelper) bind ImageRotationHelper::class
    factoryOf(::MediaPipeHandDetector) {
        named(HAND_DETECTOR_NAME)
        bind<ObjectDetector<HandDetectionResult>>()
    }
    factoryOf(::ArUcoMarkerDetector) {
        named(MARKER_DETECTOR_NAME)
        bind<ObjectDetector<MarkersDetectionResult>>()
    }
    factoryOf(::DefaultTouchPositionExtractor) bind TouchPositionExtractor::class
}
