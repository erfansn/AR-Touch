package ir.erfansn.artouch.di

import androidx.camera.core.ImageProxy
import ir.erfansn.artouch.producer.detector.ObjectDetector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object DummyObjectDetector : ObjectDetector<Any> {
    override val result: SharedFlow<Any> = MutableSharedFlow()

    override fun detect(imageProxy: ImageProxy) {
        TODO("Not yet implemented")
    }
}
