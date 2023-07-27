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
