package ir.erfansn.artouch.detector

import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

data class HandDetectionResult(
    val landmarks: HandLandmarkerResult,
    val inferenceTime: Long,
    val inputImageHeight: Int,
    val inputImageWidth: Int,
)
