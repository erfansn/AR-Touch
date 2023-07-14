package ir.erfansn.artouch.common.util

data class Point(val x: Float, val y: Float) {

    operator fun minus(other: Point) = Point(x = x - other.x, y = y - other.y)

    operator fun plus(other: Point) = Point(x = x + other.x, y = y + other.y)

    operator fun times(factor: Float) = Point(x = x * factor, y = y * factor)
}
