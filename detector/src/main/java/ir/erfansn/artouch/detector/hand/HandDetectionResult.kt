package ir.erfansn.artouch.detector.hand

import android.util.Size
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

data class HandDetectionResult(
    val inferenceTime: Long,
    val inputImageSize: Size,
    val landmarks: HandLandmarkerResult,
)
