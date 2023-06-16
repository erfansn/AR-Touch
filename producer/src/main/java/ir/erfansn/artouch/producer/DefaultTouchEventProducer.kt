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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import java.lang.Math.toDegrees
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

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
            require(hand.landmarks.isNotEmpty())
            require(marker.markers.isNotEmpty())

            hand.landmarks.first().let {
                val indexFingerMcp = it[HandLandmark.INDEX_FINGER_MCP]
                val middleFingerMcp = it[HandLandmark.MIDDLE_FINGER_MCP]
                val touchFingerMcpCenterPoint = NormalizedLandmark.create(
                    (indexFingerMcp.x() + middleFingerMcp.x()) / 2,
                    (indexFingerMcp.y() + middleFingerMcp.y()) / 2,
                    (indexFingerMcp.z() + middleFingerMcp.z()) / 2,
                )

                val indexFingerTip = it[HandLandmark.INDEX_FINGER_TIP]
                val middleFingerTip = it[HandLandmark.MIDDLE_FINGER_TIP]
                val touchFingersAngle = toDegrees(
                    abs(
                        atan2(
                            y = indexFingerTip.y() - touchFingerMcpCenterPoint.y(),
                            x = indexFingerTip.x() - touchFingerMcpCenterPoint.x()
                        ) - atan2(
                            y = middleFingerTip.y() - touchFingerMcpCenterPoint.y(),
                            x = middleFingerTip.x() - touchFingerMcpCenterPoint.x()
                        )
                    ).toDouble()
                )
                val touchFingersLength = hypot(
                    x = indexFingerTip.x() - middleFingerTip.x(),
                    y = indexFingerTip.y() - middleFingerTip.y(),
                )
                val touchFingersCenterPoint = calculateCenter(middleFingerTip, indexFingerTip)

                Log.d(TAG, "Angle between Touch fingers is $touchFingersAngle")
                Log.d(TAG, "Length between Touch fingers is $touchFingersLength")
                Log.d(TAG, "Center point between Touch fingers is $touchFingersCenterPoint")

                TouchEvent(
                    pressed = touchFingersAngle <= MIN_TOUCHING_ANGLE || touchFingersLength <= MIN_TOUCHING_LENGTH,
                    position = extractTouchPosition(
                        target = touchFingersCenterPoint,
                        boundary = marker.markers,
                    )
                )
            }
        }
        .retryWith(TouchEvent.RELEASE)
        .map {
            if (!it.pressed) return@map it

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

        private const val TOLERANCE = 0.3f
        private const val MIN_TOUCHING_ANGLE = 10f
        private const val MIN_TOUCHING_LENGTH = 0.0385f
    }
}

private fun <T> Flow<T>.retryWith(value: T): Flow<T> {
    return retryWhen { _, _ ->
        emit(value)
        true
    }
}
