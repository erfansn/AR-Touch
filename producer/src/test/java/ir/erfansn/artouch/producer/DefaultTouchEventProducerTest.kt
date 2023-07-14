package ir.erfansn.artouch.producer

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmark
import ir.erfansn.artouch.producer.detector.hand.HandDetectionResult
import ir.erfansn.artouch.producer.detector.marker.MarkersDetectionResult
import ir.erfansn.artouch.producer.extractor.TouchPositionExtractor
import ir.erfansn.artouch.common.util.Point
import ir.erfansn.artouch.common.util.Size
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.tan
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultTouchEventProducerTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val initHandResult = HandDetectionResult(
        inferenceTime = 0,
        inputImageSize = Size(1, 1),
        landmarks = listOf(NormalizedLandmark.create(0f, 0f, 0f))
    )
    private val initMarkersResult = MarkersDetectionResult(
        inferenceTime = 0,
        inputImageSize = Size(1, 1),
        positions = arrayOf(Point(0f, 0f))
    )
    private val handDetectionResult = MutableStateFlow(initHandResult)
    private val markersDetectionResult = MutableStateFlow(initMarkersResult)
    private lateinit var touchEventProducer: TouchEventProducer

    @Before
    fun setUp() {
        val stubTouchPositionExtractor = object : TouchPositionExtractor {
            override fun extract(target: Point, boundary: Array<Point>) =
                Point(-1f, -1f)
        }
        touchEventProducer = DefaultTouchEventProducer(
            handDetectionResult = handDetectionResult,
            markersDetectionResult = markersDetectionResult,
            touchPositionExtractor = stubTouchPositionExtractor,
            defaultDispatcher = testDispatcher,
        )
    }

    @After
    fun tearDown() {
        handDetectionResult.value = initHandResult
        markersDetectionResult.value = initMarkersResult
    }

    @Test
    fun producesReleasedEvent_ifAnalysisBeInDifferentAspectRatio() = runTest(testDispatcher) {
        handDetectionResult.update {
            it.copy(inputImageSize = Size(4, 3))
        }
        markersDetectionResult.update {
            it.copy(inputImageSize = Size(16, 9))
        }

        // Because infinite retry used in this flow single
        // doesn't work due infinite suspension of collection
        val touchEvent = touchEventProducer.touchEvent.first()

        assertFalse(touchEvent.pressed)
    }

    @Test
    fun producesReleasedEvent_withoutMarkers() = runTest(testDispatcher) {
        markersDetectionResult.update {
            it.copy(positions = emptyArray())
        }

        val touchEvent = touchEventProducer.touchEvent.first()

        assertFalse(touchEvent.pressed)
    }

    @Test
    fun producesReleasedEvent_withoutHand() = runTest(testDispatcher) {
        handDetectionResult.update {
            it.copy(landmarks = emptyList())
        }

        val touchEvent = touchEventProducer.touchEvent.first()

        assertFalse(touchEvent.pressed)
    }

    @Test
    fun producesReleasedEvent_whenFingersNotCloseEnough() = runTest(testDispatcher) {
        handDetectionResult.update {
            it.copy(
                landmarks = createFakeHandLandmarks(
                    touchFingersLength = 0.05f,
                    touchFingersAngleDegrees = 18.0
                )
            )
        }

        val touchEvent = touchEventProducer.touchEvent.first()

        assertFalse(touchEvent.pressed)
    }

    @Test
    fun producesPressedEvent_whenFingersClosedEnough() = runTest(testDispatcher) {
        handDetectionResult.update {
            it.copy(
                landmarks = createFakeHandLandmarks(
                    touchFingersLength = 0.031f,
                    touchFingersAngleDegrees = 10.0,
                )
            )
        }

        val touchEvent = touchEventProducer.touchEvent.first()

        assertTrue(touchEvent.pressed)
    }

    @Test
    fun producesReleasedEvent_whenFingersNotClosedEnoughInsideBigFrame() = runTest(testDispatcher) {
        handDetectionResult.update {
            it.copy(
                landmarks = createFakeHandLandmarks(
                    touchFingersLength = 0.003f,
                    touchFingersAngleDegrees = 18.0,
                    inFarDistance = true,
                )
            )
        }

        val touchEvent = touchEventProducer.touchEvent.first()

        assertFalse(touchEvent.pressed)
    }

    @Test
    fun producesPressedEvent_whenFingersClosedEnoughInsideBigFrame() = runTest(testDispatcher) {
        handDetectionResult.update {
            it.copy(
                landmarks = createFakeHandLandmarks(
                    touchFingersLength = 0.003f,
                    touchFingersAngleDegrees = 10.0,
                    inFarDistance = true
                )
            )
        }

        val touchEvent = touchEventProducer.touchEvent.first()

        assertTrue(touchEvent.pressed)
    }

    @Test
    fun producesReleasedEvent_whenFingersNotClosedEnoughInsideSmallFrame() = runTest(testDispatcher) {
        handDetectionResult.update {
            it.copy(
                landmarks = createFakeHandLandmarks(
                    touchFingersLength = 0.05f,
                    touchFingersAngleDegrees = 20.0,
                    inFarDistance = false
                )
            )
        }

        val touchEvent = touchEventProducer.touchEvent.first()

        assertFalse(touchEvent.pressed)
    }

    @Test
    fun producesPressedEvent_whenFingersClosedEnoughInsideSmallFrame() = runTest(testDispatcher) {
        handDetectionResult.update {
            it.copy(
                landmarks = createFakeHandLandmarks(
                    touchFingersLength = 0.05f,
                    touchFingersAngleDegrees = 9.0,
                    inFarDistance = false
                )
            )
        }

        val touchEvent = touchEventProducer.touchEvent.first()

        assertTrue(touchEvent.pressed)
    }

    private fun createFakeHandLandmarks(
        touchFingersLength: Float,
        touchFingersAngleDegrees: Double,
        inFarDistance: Boolean? = null,
    ): List<NormalizedLandmark> {
        val randomPoint =
            Point(Random.nextFloat(), Random.nextFloat())
        val otherPoint = randomPoint + Point(touchFingersLength, 0f)

        val vertexLength = touchFingersLength / tan(Math.toRadians(touchFingersAngleDegrees))
        val vertex = otherPoint - Point(0f, vertexLength.toFloat())

        val handLandmarks = mutableListOf<NormalizedLandmark>()
        repeat(HandLandmark.NUM_LANDMARKS) {
            handLandmarks += when(it) {
                HandLandmark.INDEX_FINGER_TIP -> {
                    NormalizedLandmark.create(randomPoint.x, randomPoint.y, 0f)
                }
                HandLandmark.MIDDLE_FINGER_TIP -> {
                    NormalizedLandmark.create(otherPoint.x, otherPoint.y, 0f)
                }
                HandLandmark.INDEX_FINGER_MCP -> {
                    NormalizedLandmark.create(vertex.x + 0.02f, vertex.y, 0f)
                }
                HandLandmark.MIDDLE_FINGER_MCP -> {
                    NormalizedLandmark.create(vertex.x - 0.02f, vertex.y, 0f)
                }
                HandLandmark.WRIST -> {
                    NormalizedLandmark.create(0f, 0f, inFarDistance?.let {
                        if (it) 0f else 1f
                    } ?: Random.nextFloat())
                }
                else -> {
                    NormalizedLandmark.create(0f, 0f, 0f)
                }
            }
        }
        return handLandmarks
    }
}
