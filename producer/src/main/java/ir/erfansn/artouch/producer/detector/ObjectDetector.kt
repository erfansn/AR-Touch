package ir.erfansn.artouch.producer.detector

import androidx.camera.core.ImageProxy
import kotlinx.coroutines.flow.SharedFlow

interface ObjectDetector<T> {
    val result: SharedFlow<T>
    fun detect(imageProxy: ImageProxy)
}
