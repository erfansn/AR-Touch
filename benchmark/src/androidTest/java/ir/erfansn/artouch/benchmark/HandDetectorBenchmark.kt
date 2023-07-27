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

package ir.erfansn.artouch.benchmark

import android.content.Context
import android.graphics.PixelFormat
import androidx.benchmark.junit4.BenchmarkRule
import androidx.test.core.app.ApplicationProvider
import ir.erfansn.artouch.benchmark.di.testModule
import ir.erfansn.artouch.benchmark.util.loadJpegImageIntoImageProxy
import ir.erfansn.artouch.producer.detector.ObjectDetector
import ir.erfansn.artouch.producer.detector.hand.HandDetectionResult
import ir.erfansn.artouch.producer.di.HAND_DETECTOR_QUALIFIER
import ir.erfansn.artouch.producer.di.producerModule
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runners.MethodSorters
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get

// If you have a Xiaomi device and cannot run the tests, follow the steps below:
//  1. Click gutter icon to run whole tests
//  2. When running a_fakeTest enter follow command in terminal
//  "adb shell am start -n 'ir.erfansn.artouch.benchmark.test/androidx.test.core.app.InstrumentationActivityInvoker\$BootstrapActivity'"
//  3. Wait to complete tests
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class HandDetectorBenchmark : KoinTest {

    @get:Rule(order = 0)
    val koinTestRule = KoinTestRule.create {
        modules(producerModule, testModule)
    }

    @get:Rule(order = 1)
    val benchmarkRule = BenchmarkRule()

    @Test
    fun a_fakeTest() = Unit

    @Test
    fun detectingBenchmark() = runBlocking {
        val handLandmarkerDetector = get<ObjectDetector<HandDetectionResult>>(HAND_DETECTOR_QUALIFIER)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val handImage = context.loadJpegImageIntoImageProxy("hand.jpg", format = PixelFormat.RGBA_8888)

        benchmarkRule.measureRepeated {
            val result = async { handLandmarkerDetector.result.first() }
            handLandmarkerDetector.detect(handImage)
            result.await()
        }
    }
}

private inline fun BenchmarkRule.measureRepeated(block: BenchmarkRule.Scope.() -> Unit) {
    // Note: this is an extension function to discourage calling from Java.

    // Extract members to locals, to ensure we check #applied, and we don't hit accessors
    val localState = getState()
    val localScope = scope

    while (localState.keepRunningInline()) {
        block(localScope)
    }
}
