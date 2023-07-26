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

package ir.erfansn.artouch.di

import ir.erfansn.artouch.common.util.Point
import ir.erfansn.artouch.producer.extractor.TouchPositionExtractor

object DummyTouchPositionExtractor : TouchPositionExtractor {
    override fun extract(target: Point, boundary: Array<Point>): Point {
        TODO("Not yet implemented")
    }
}
