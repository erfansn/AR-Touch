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

package ir.erfansn.artouch.producer.di

import ir.erfansn.artouch.producer.detector.ObjectDetector
import ir.erfansn.artouch.producer.detector.hand.HandDetectionResult
import ir.erfansn.artouch.producer.detector.hand.HandLandmarkerDetector
import ir.erfansn.artouch.producer.detector.aruco.ArUcoMarkerDetector
import ir.erfansn.artouch.producer.detector.aruco.ArUcoDetectionResult
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
private const val ARUCO_DETECTOR_NAME = "aruco_detector"

val HAND_DETECTOR_QUALIFIER = qualifier(HAND_DETECTOR_NAME)
val ARUCO_DETECTOR_QUALIFIER = qualifier(ARUCO_DETECTOR_NAME)

val producerModule = module {
    single { CoroutineScope(Dispatchers.Default) }

    factoryOf(::DefaultImageRotationHelper) bind ImageRotationHelper::class
    factoryOf(::HandLandmarkerDetector) {
        named(HAND_DETECTOR_NAME)
        bind<ObjectDetector<HandDetectionResult>>()
    }
    factoryOf(::ArUcoMarkerDetector) {
        named(ARUCO_DETECTOR_NAME)
        bind<ObjectDetector<ArUcoDetectionResult>>()
    }
    factoryOf(::DefaultTouchPositionExtractor) bind TouchPositionExtractor::class
}
