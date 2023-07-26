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

package ir.erfansn.artouch.producer.detector.aruco

import ir.erfansn.artouch.common.util.Point
import ir.erfansn.artouch.common.util.Size

data class ArUcoDetectionResult(
    val inferenceTime: Long,
    val inputImageSize: Size,
    val positions: Array<Point>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArUcoDetectionResult

        if (inferenceTime != other.inferenceTime) return false
        if (inputImageSize != other.inputImageSize) return false
        if (!positions.contentEquals(other.positions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = inferenceTime.hashCode()
        result = 31 * result + inputImageSize.hashCode()
        result = 31 * result + positions.contentHashCode()
        return result
    }
}
