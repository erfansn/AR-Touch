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
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get

class HandDetectorBenchmark : KoinTest {

    @get:Rule(order = 0)
    val koinTestRule = KoinTestRule.create {
        modules(producerModule, testModule)
    }

    @get:Rule(order = 1)
    val benchmarkRule = BenchmarkRule()

    @Test
    fun detectingBenchmark() = runBlocking {
        val handLandmarkerDetector = get<ObjectDetector<HandDetectionResult>>(HAND_DETECTOR_QUALIFIER)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val handImage = context.loadJpegImageIntoImageProxy("hand.jpg", format = PixelFormat.RGBA_8888)

        benchmarkRule.measureRepeated {
            val job = launch {
                handLandmarkerDetector.result.first()
            }
            handLandmarkerDetector.detect(handImage)
            job.cancelAndJoin()
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
