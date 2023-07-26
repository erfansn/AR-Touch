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

package ir.erfansn.artouch.benchmark

import android.content.Context
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.core.app.ApplicationProvider
import ir.erfansn.artouch.benchmark.di.testModule
import ir.erfansn.artouch.benchmark.util.loadJpegImageIntoImageProxy
import ir.erfansn.artouch.producer.detector.ObjectDetector
import ir.erfansn.artouch.producer.detector.aruco.ArUcoDetectionResult
import ir.erfansn.artouch.producer.di.ARUCO_DETECTOR_QUALIFIER
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
class ArUcoDetectorBenchmark : KoinTest {

    @get:Rule(order = 0)
    val koinTestRule = KoinTestRule.create {
        modules(producerModule, testModule)
    }

    @get:Rule(order = 1)
    val benchmarkRule = BenchmarkRule()

    @Test
    fun a_fakeTest() = Unit

    @Test
    fun detectingBenchmark() {
        val arUcoMarkerDetector = get<ObjectDetector<ArUcoDetectionResult>>(ARUCO_DETECTOR_QUALIFIER)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val arucoImage = context.loadJpegImageIntoImageProxy("aruco.jpg")

        benchmarkRule.measureRepeated {
            arUcoMarkerDetector.detect(arucoImage)
        }
    }
}
