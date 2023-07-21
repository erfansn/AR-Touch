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
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get

class ArUcoDetectorBenchmark : KoinTest {

    @get:Rule(order = 0)
    val koinTestRule = KoinTestRule.create {
        modules(producerModule, testModule)
    }

    @get:Rule(order = 1)
    val benchmarkRule = BenchmarkRule()

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
