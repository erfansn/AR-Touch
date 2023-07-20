package ir.erfansn.artouch.producer

import android.util.Log
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmark
import ir.erfansn.artouch.common.util.Point
import ir.erfansn.artouch.common.util.Size
import ir.erfansn.artouch.producer.detector.aruco.ArUcoDetectionResult
import ir.erfansn.artouch.producer.detector.hand.HandDetectionResult
import ir.erfansn.artouch.producer.extractor.TouchPositionExtractor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

class DefaultTouchEventProducer(
    handDetectionResult: Flow<HandDetectionResult>,
    arUcoDetectionResult: Flow<ArUcoDetectionResult>,
    touchPositionExtractor: TouchPositionExtractor,
    defaultDispatcher: CoroutineDispatcher,
) : TouchEventProducer {

    private var previousTouchPosition = Point(0f, 0f)

    override val touchEvent = handDetectionResult
        .combine(arUcoDetectionResult) { hand, aruco ->
            require(isSameAspectRatio(hand.inputImageSize, aruco.inputImageSize))
            require(hand.landmarks.isNotEmpty())
            require(aruco.positions.isNotEmpty())

            hand.landmarks.let {
                val (touchFingerMcpX, touchFingerMcpY) = calculateCenter(
                    it[HandLandmark.INDEX_FINGER_MCP],
                    it[HandLandmark.MIDDLE_FINGER_MCP]
                )

                val indexFingerTip = it[HandLandmark.INDEX_FINGER_TIP]
                val middleFingerTip = it[HandLandmark.MIDDLE_FINGER_TIP]
                val touchFingersAngle = Math.toDegrees(
                    abs(
                        atan2(
                            y = indexFingerTip.y() - touchFingerMcpY,
                            x = indexFingerTip.x() - touchFingerMcpX
                        ) - atan2(
                            y = middleFingerTip.y() - touchFingerMcpY,
                            x = middleFingerTip.x() - touchFingerMcpX
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
                Log.d(TAG, "Z coordination of wrist is ${it[HandLandmark.WRIST].z()}")

                TouchEvent(
                    pressed = (it[HandLandmark.WRIST].z() > MAX_HAND_DEPTH && touchFingersLength <= MAX_FINGERS_LENGTH) || touchFingersAngle <= MAX_FINGERS_ANGLE,
                    position = touchPositionExtractor.extract(
                        target = touchFingersCenterPoint,
                        boundary = aruco.positions,
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
        .flowOn(defaultDispatcher)
        .distinctUntilChanged()

    private fun isSameAspectRatio(first: Size, second: Size): Boolean {
        return (first.width / second.width) == (first.height / second.height)
    }

    private fun calculateCenter(first: NormalizedLandmark, second: NormalizedLandmark): Point {
        return Point(
            (first.x() + second.x()) / 2f,
            (first.y() + second.y()) / 2f
        )
    }

    companion object {
        private const val TAG = "DefaultTouchEventProducer"

        private const val TOLERANCE = 0.35f
        private const val MAX_FINGERS_ANGLE = 12f
        private const val MAX_FINGERS_LENGTH = 0.0385f
        private const val MAX_HAND_DEPTH = 1.5739107E-7f
    }
}

private fun <T> Flow<T>.retryWith(value: T): Flow<T> {
    return retryWhen { _, _ ->
        emit(value)
        true
    }
}
