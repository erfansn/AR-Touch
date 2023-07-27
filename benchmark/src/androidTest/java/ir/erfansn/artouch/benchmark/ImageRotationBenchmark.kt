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
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.core.app.ApplicationProvider
import ir.erfansn.artouch.benchmark.util.loadJpegImageAsBitmap
import ir.erfansn.artouch.benchmark.util.toYuvPlanes
import ir.erfansn.artouch.producer.detector.util.ImageRotationHelper
import ir.erfansn.artouch.producer.di.producerModule
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
class ImageRotationBenchmark : KoinTest {

    @get:Rule(order = 0)
    val koinTestRule = KoinTestRule.create {
        modules(producerModule)
    }

    @get:Rule(order = 1)
    val benchmarkRule = BenchmarkRule()

    @Test
    fun a_fakeTest() = Unit

    @Test
    fun bitmapRotationBenchmark() {
        val imageRotationHelper = get<ImageRotationHelper>()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val image = context.loadJpegImageAsBitmap("aruco.jpg")

        benchmarkRule.measureRepeated {
            with(imageRotationHelper) {
                image.rotate(90)
            }
        }
    }

    @Test
    fun bufferRotationBenchmark() {
        val imageRotationHelper = get<ImageRotationHelper>()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val (yBuffer, _, _)  = context.loadJpegImageAsBitmap("aruco.jpg").toYuvPlanes()

        benchmarkRule.measureRepeated {
            with(imageRotationHelper) {
                yBuffer.buffer.rotate(
                    degrees = 90,
                    rowStride = yBuffer.rowStride
                )
            }
        }
    }
}
