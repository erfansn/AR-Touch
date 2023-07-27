/*
 * Copyright 2023 Erfan Sn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ir.erfansn.artouch.producer

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmark
import ir.erfansn.artouch.common.util.Point
import ir.erfansn.artouch.common.util.Size
import ir.erfansn.artouch.producer.detector.aruco.ArUcoDetectionResult
import ir.erfansn.artouch.producer.detector.hand.HandDetectionResult
import ir.erfansn.artouch.producer.extractor.StubTouchPositionExtractor
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

    private val initHandDetectionResult = HandDetectionResult(
        inferenceTime = 0,
        inputImageSize = Size(1, 1),
        landmarks = listOf(NormalizedLandmark.create(0f, 0f, 0f))
    )
    private val initArUcoDetectionResult = ArUcoDetectionResult(
        inferenceTime = 0,
        inputImageSize = Size(1, 1),
        positions = arrayOf(Point(0f, 0f))
    )
    private val handDetectionResult = MutableStateFlow(initHandDetectionResult)
    private val arucoDetectionResult = MutableStateFlow(initArUcoDetectionResult)
    private lateinit var touchEventProducer: TouchEventProducer

    @Before
    fun setUp() {
        touchEventProducer = DefaultTouchEventProducer(
            handDetectionResult = handDetectionResult,
            arUcoDetectionResult = arucoDetectionResult,
            touchPositionExtractor = StubTouchPositionExtractor,
            defaultDispatcher = testDispatcher,
        )
    }

    @After
    fun tearDown() {
        handDetectionResult.value = initHandDetectionResult
        arucoDetectionResult.value = initArUcoDetectionResult
    }

    @Test
    fun producesReleasedEvent_ifAnalysisBeInDifferentAspectRatio() = runTest(testDispatcher) {
        handDetectionResult.update {
            it.copy(inputImageSize = Size(4, 3))
        }
        arucoDetectionResult.update {
            it.copy(inputImageSize = Size(16, 9))
        }

        // Because infinite retry used in this flow single
        // doesn't work due infinite suspension of collection
        val touchEvent = touchEventProducer.touchEvent.first()

        assertFalse(touchEvent.pressed)
    }

    @Test
    fun producesReleasedEvent_withoutMarkers() = runTest(testDispatcher) {
        arucoDetectionResult.update {
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
    fun producesReleasedEvent_whenFingersNotClosedEnoughInsideBigFrame() = runTest(testDispatcher) {
        handDetectionResult.update {
            it.copy(
                landmarks = createFakeHandLandmarks(
                    hasSmallHand = true,
                    fingersClosed = false,
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
                    hasSmallHand = true,
                    fingersClosed = true,
                )
            )
        }

        val touchEvent = touchEventProducer.touchEvent.first()

        assertTrue(touchEvent.pressed)
    }

    @Test
    fun producesReleasedEvent_whenFingersNotClosedEnoughInsideSmallFrame() =
        runTest(testDispatcher) {
            handDetectionResult.update {
                it.copy(
                    landmarks = createFakeHandLandmarks(
                        hasSmallHand = false,
                        fingersClosed = false,
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
                    hasSmallHand = false,
                    fingersClosed = true,
                )
            )
        }

        val touchEvent = touchEventProducer.touchEvent.first()

        assertTrue(touchEvent.pressed)
    }

    private fun createFakeHandLandmarks(
        hasSmallHand: Boolean,
        fingersClosed: Boolean,
    ): List<NormalizedLandmark> {
        val inFarDistance: Boolean = hasSmallHand
        val (touchFingersLength, touchFingersAngleDegrees) = when {
            hasSmallHand && fingersClosed -> 0.0001f to 1.0
            hasSmallHand && !fingersClosed -> 0.0001f to 40.0
            !hasSmallHand && fingersClosed -> 0.1f to 1.0
            else -> 0.1f to 40.0
        }

        val randomPoint = Point(Random.nextFloat(), Random.nextFloat())
        val otherPoint = randomPoint + Point(touchFingersLength, 0f)

        val vertexLength = touchFingersLength / tan(Math.toRadians(touchFingersAngleDegrees))
        val vertex = otherPoint - Point(0f, vertexLength.toFloat())

        val handLandmarks = mutableListOf<NormalizedLandmark>()
        repeat(HandLandmark.NUM_LANDMARKS) {
            handLandmarks += when (it) {
                HandLandmark.INDEX_FINGER_TIP -> {
                    NormalizedLandmark.create(randomPoint.x, randomPoint.y, 0f)
                }

                HandLandmark.MIDDLE_FINGER_TIP -> {
                    NormalizedLandmark.create(otherPoint.x, otherPoint.y, 0f)
                }

                HandLandmark.INDEX_FINGER_MCP -> {
                    NormalizedLandmark.create(vertex.x + 0.002f, vertex.y, 0f)
                }

                HandLandmark.MIDDLE_FINGER_MCP -> {
                    NormalizedLandmark.create(vertex.x - 0.002f, vertex.y, 0f)
                }

                HandLandmark.WRIST -> {
                    NormalizedLandmark.create(0f, 0f, if (inFarDistance) 0f else 1f)
                }

                else -> {
                    NormalizedLandmark.create(0f, 0f, 0f)
                }
            }
        }
        return handLandmarks
    }
}
