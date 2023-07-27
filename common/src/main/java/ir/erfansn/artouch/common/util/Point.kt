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

package ir.erfansn.artouch.common.util

data class Point(val x: Float, val y: Float) {

    operator fun minus(other: Point) = Point(x = x - other.x, y = y - other.y)

    operator fun plus(other: Point) = Point(x = x + other.x, y = y + other.y)

    operator fun times(factor: Float) = Point(x = x * factor, y = y * factor)
}
