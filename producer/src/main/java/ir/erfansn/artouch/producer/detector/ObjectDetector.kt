package ir.erfansn.artouch.producer.detector

import androidx.camera.core.ImageProxy
import kotlinx.coroutines.flow.Flow

interface ObjectDetector<T> {
    val result: Flow<T>
    fun detect(imageProxy: ImageProxy)
}
