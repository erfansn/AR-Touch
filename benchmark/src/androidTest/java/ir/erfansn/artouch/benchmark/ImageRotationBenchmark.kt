package ir.erfansn.artouch.benchmark

import android.content.Context
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.core.app.ApplicationProvider
import ir.erfansn.artouch.benchmark.util.loadJpegImageAsBitmap
import ir.erfansn.artouch.benchmark.util.toYuvPlanes
import ir.erfansn.artouch.producer.detector.util.ImageRotationHelper
import ir.erfansn.artouch.producer.di.producerModule
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get

class ImageRotationBenchmark : KoinTest {

    @get:Rule(order = 0)
    val koinTestRule = KoinTestRule.create {
        modules(producerModule)
    }

    @get:Rule(order = 1)
    val benchmarkRule = BenchmarkRule()

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
