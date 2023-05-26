package ir.erfansn.artouch.detector

import android.graphics.PointF
import android.util.Log
import android.util.Size
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmark
import ir.erfansn.artouch.detector.hand.HandDetectionResult
import ir.erfansn.artouch.detector.marker.MarkerDetectionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlin.math.abs
import kotlin.math.atan2

class DefaultTouchPositionExtractor(
    handDetector: ObjectDetector<HandDetectionResult>,
    markerDetector: ObjectDetector<MarkerDetectionResult>,
) : TouchPositionExtractor {

    init {
        System.loadLibrary("touch_position_extractor")
    }

    override val touchPosition = handDetector.result
        .combine(markerDetector.result) { hand, marker ->
            require(isSameAspectRatio(hand.inputImageSize, marker.inputImageSize))
            require(marker.markers.size == 4)

            val handLandmarks = hand.landmarks.firstOrNull() ?: return@combine PointF(0f, 0f)
            val touchFingersAngle = handLandmarks.let {
                val vertex = it[HandLandmark.WRIST]

                abs(
                    Math.toDegrees(
                        atan2(
                            it[HandLandmark.INDEX_FINGER_TIP].y() - vertex.y(),
                            it[HandLandmark.INDEX_FINGER_TIP].x() - vertex.x()
                        ).toDouble() - atan2(
                            it[HandLandmark.THUMB_TIP].y() - vertex.y(),
                            it[HandLandmark.THUMB_TIP].x() - vertex.x()
                        )
                    )
                )
            }
            val centerTouchFingersPoint = handLandmarks.let {
                calculateCenter(it[HandLandmark.THUMB_TIP], it[HandLandmark.INDEX_FINGER_TIP]).also {
                    Log.d(TAG, "Center's Touch fingers is $it")
                }
            }

            Log.d(TAG, "Center point between Touch fingers is $centerTouchFingersPoint")
            Log.d(TAG, "Angle between Touch fingers is $touchFingersAngle")
            if (touchFingersAngle <= MIN_TOUCHING_ANGLE) {
                extractTouchPosition(
                    target = centerTouchFingersPoint,
                    boundary = marker.markers,
                )
            } else {
                PointF(0f, 0f)
            }
        }
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged()

    private fun isSameAspectRatio(first: Size, second: Size): Boolean {
        return (first.width.toFloat() / second.width) == (first.height.toFloat() / second.height)
    }

    private fun calculateCenter(first: NormalizedLandmark, second: NormalizedLandmark): PointF {
        return PointF(abs(first.x() + second.x()) / 2f, abs(first.y() + second.y()) / 2f)
    }

    private external fun extractTouchPosition(
        target: PointF,
        boundary: Array<PointF>,
    ): PointF

    companion object {
        private const val TAG = "DefaultTouchPositionExtractor"

        private const val MIN_TOUCHING_ANGLE = 8
    }
}
