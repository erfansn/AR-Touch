package ir.erfansn.artouch.producer.extractor

import ir.erfansn.artouch.common.util.Point
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class DefaultTouchPositionExtractorTest {

    private val touchPositionExtractor = DefaultTouchPositionExtractor()

    @Test
    fun extractsCorrectPosition_whenTargetIsInsideOfBoundary() {
        val target = Point(0.375f, 0.5f)
        val boundary = arrayOf(
            Point(0.25f, 0.25f),
            Point(0.75f, 0.25f),
            Point(0.75f, 0.75f),
            Point(0.25f, 0.75f),
        )

        val touchPosition = touchPositionExtractor.extract(target, boundary)

        assertEquals(touchPosition, Point(0.25f, 0.5f))
    }

    @Test
    fun extractsPositionZeroZero_whenTargetIsOutTopLeftOfBoundary() {
        val target = Point(0.125f, 0.125f)
        val boundary = arrayOf(
            Point(0.25f, 0.25f),
            Point(0.75f, 0.25f),
            Point(0.75f, 0.75f),
            Point(0.25f, 0.75f),
        )

        val touchPosition = touchPositionExtractor.extract(target, boundary)

        assertEquals(touchPosition, Point(0.0f, 0.0f))
    }

    @Test
    fun extractsCorrectPositionWithYZero_whenTargetIsOutTopOfBoundary() {
        val target = Point(0.5f, 0.125f)
        val boundary = arrayOf(
            Point(0.25f, 0.25f),
            Point(0.75f, 0.25f),
            Point(0.75f, 0.75f),
            Point(0.25f, 0.75f),
        )

        val touchPosition = touchPositionExtractor.extract(target, boundary)

        assertEquals(touchPosition, Point(0.5f, 0.0f))
    }

    @Test
    fun extractsPositionOneZero_whenTargetIsOutTopRightOfBoundary() {
        val target = Point(0.875f, 0.125f)
        val boundary = arrayOf(
            Point(0.25f, 0.25f),
            Point(0.75f, 0.25f),
            Point(0.75f, 0.75f),
            Point(0.25f, 0.75f),
        )

        val touchPosition = touchPositionExtractor.extract(target, boundary)

        assertEquals(touchPosition, Point(1.0f, 0.0f))
    }

    @Test
    fun extractsCorrectPositionWithXZero_whenTargetIsOutLeftOfBoundary() {
        val target = Point(0.125f, 0.5f)
        val boundary = arrayOf(
            Point(0.25f, 0.25f),
            Point(0.75f, 0.25f),
            Point(0.75f, 0.75f),
            Point(0.25f, 0.75f),
        )

        val touchPosition = touchPositionExtractor.extract(target, boundary)

        assertEquals(touchPosition, Point(0.0f, 0.5f))
    }

    @Test
    fun extractsCorrectPositionWithXOne_whenTargetIsOutRightOfBoundary() {
        val target = Point(0.875f, 0.5f)
        val boundary = arrayOf(
            Point(0.25f, 0.25f),
            Point(0.75f, 0.25f),
            Point(0.75f, 0.75f),
            Point(0.25f, 0.75f),
        )

        val touchPosition = touchPositionExtractor.extract(target, boundary)

        assertEquals(touchPosition, Point(1.0f, 0.5f))
    }

    @Test
    fun extractsPositionZeroOne_whenTargetIsOutBottomLeftOfBoundary() {
        val target = Point(0.125f, 0.875f)
        val boundary = arrayOf(
            Point(0.25f, 0.25f),
            Point(0.75f, 0.25f),
            Point(0.75f, 0.75f),
            Point(0.25f, 0.75f),
        )

        val touchPosition = touchPositionExtractor.extract(target, boundary)

        assertEquals(touchPosition, Point(0.0f, 1.0f))
    }

    @Test
    fun extractsCorrectPositionWithOneY_whenTargetIsOutBottomOfBoundary() {
        val target = Point(0.5f, 0.875f)
        val boundary = arrayOf(
            Point(0.25f, 0.25f),
            Point(0.75f, 0.25f),
            Point(0.75f, 0.75f),
            Point(0.25f, 0.75f),
        )

        val touchPosition = touchPositionExtractor.extract(target, boundary)

        assertEquals(touchPosition, Point(0.5f, 1.0f))
    }

    @Test
    fun extractsPositionOneOne_whenTargetIsOutBottomRightOfBoundary() {
        val target = Point(0.875f, 0.875f)
        val boundary = arrayOf(
            Point(0.25f, 0.25f),
            Point(0.75f, 0.25f),
            Point(0.75f, 0.75f),
            Point(0.25f, 0.75f),
        )

        val touchPosition = touchPositionExtractor.extract(target, boundary)

        assertEquals(touchPosition, Point(1.0f, 1.0f))
    }
}
