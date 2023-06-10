package ir.erfansn.artouch.producer

import android.graphics.PointF
import android.util.Log
import android.util.Size
import androidx.core.graphics.minus
import androidx.core.graphics.plus
import androidx.core.graphics.times
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmark
import ir.erfansn.artouch.producer.detector.ObjectDetector
import ir.erfansn.artouch.producer.detector.hand.HandDetectionResult
import ir.erfansn.artouch.producer.detector.marker.MarkerDetectionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.lang.Math.toDegrees
import kotlin.math.abs
import kotlin.math.atan2

class DefaultTouchEventProducer(
    handDetector: ObjectDetector<HandDetectionResult>,
    markerDetector: ObjectDetector<MarkerDetectionResult>,
) : TouchEventProducer {

    init {
        System.loadLibrary("touch_position_extractor")
    }

    private var previousTouchPosition = PointF(0f, 0f)

    override val touchEvent = handDetector.result
        .combine(markerDetector.result) { hand, marker ->
            require(isSameAspectRatio(hand.inputImageSize, marker.inputImageSize))
            require(marker.markers.size == 4)

            hand.landmarks.firstOrNull()?.let {
                Log.d(TAG, it.joinToString())

                val wrist = it[HandLandmark.WRIST]
                val indexFingerTip = it[HandLandmark.INDEX_FINGER_TIP]
                val thumbTip = it[HandLandmark.THUMB_TIP]

                val touchFingersAngle = toDegrees(
                    abs(
                        atan2(
                            y = indexFingerTip.y() - wrist.y(),
                            x = indexFingerTip.x() - wrist.x()
                        ) - atan2(
                            y = thumbTip.y() - wrist.y(),
                            x = thumbTip.x() - wrist.x()
                        )
                    ).toDouble()
                )
                val centerTouchFingersPoint = calculateCenter(thumbTip, indexFingerTip).also {
                    Log.d(TAG, "Center's Touch fingers is $it")
                }

                Log.d(TAG, "Center point between Touch fingers is $centerTouchFingersPoint")
                Log.d(TAG, "Angle between Touch fingers is $touchFingersAngle")

                TouchEvent(
                    pressed = touchFingersAngle <= MIN_TOUCHING_ANGLE,
                    position = extractTouchPosition(
                        target = centerTouchFingersPoint,
                        boundary = marker.markers,
                    )
                )
            } ?: run {
                TouchEvent.RELEASE
            }
        }.map {
            if (it == TouchEvent.RELEASE || previousTouchPosition == PointF(0f, 0f)) return@map it

            it.copy(
                position = previousTouchPosition + (it.position - previousTouchPosition) * TOLERANCE
            )
        }.onEach {
            previousTouchPosition = it.position
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
        private const val TAG = "DefaultTouchEventProducer"

        private const val TOLERANCE = 0.125f
        private const val MIN_TOUCHING_ANGLE = 8
    }
}
