package ir.erfansn.artouch.producer.detector.hand

import android.util.Size
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

data class HandDetectionResult(
    val inferenceTime: Long,
    val inputImageSize: Size,
    val landmarks: List<List<NormalizedLandmark>>,
)
