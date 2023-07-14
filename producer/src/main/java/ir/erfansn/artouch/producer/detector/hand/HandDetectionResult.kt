package ir.erfansn.artouch.producer.detector.hand

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import ir.erfansn.artouch.common.util.Size

data class HandDetectionResult(
    val inferenceTime: Long,
    val inputImageSize: Size,
    val landmarks: List<List<NormalizedLandmark>>,
)
