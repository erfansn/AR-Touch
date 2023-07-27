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

internal class DefaultTouchPositionExtractor : TouchPositionExtractor {

    init {
        System.loadLibrary("touch_position_extractor")
    }

    override fun extract(
        target: Point,
        boundary: Array<Point>,
    ): Point {
        require(boundary.size == 4)

        return extractTouchPosition(target, boundary)
    }

    private external fun extractTouchPosition(
        target: Point,
        boundary: Array<Point>,
    ): Point
}
