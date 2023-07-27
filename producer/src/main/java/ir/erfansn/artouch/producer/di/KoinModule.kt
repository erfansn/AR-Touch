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
