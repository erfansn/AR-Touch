/*
 * Copyright (C) 2023 ErfanSn
 *
 * AR Touch is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AR Touch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AR Touch.  If not, see <https://www.gnu.org/licenses/>.
 */

package ir.erfansn.artouch.producer.extractor

import ir.erfansn.artouch.common.util.Point
import org.junit.Assert.*
import org.junit.Test

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
