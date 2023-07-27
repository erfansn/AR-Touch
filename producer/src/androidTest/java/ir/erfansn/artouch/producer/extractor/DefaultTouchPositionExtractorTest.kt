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
