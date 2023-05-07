package ir.erfansn.artouch.detector

import androidx.camera.core.ImageProxy
import kotlinx.coroutines.flow.Flow

interface HandDetector {
    val result: Flow<HandDetectionResult>
    fun detect(imageProxy: ImageProxy)
}
