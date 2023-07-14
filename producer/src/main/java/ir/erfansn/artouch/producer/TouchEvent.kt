package ir.erfansn.artouch.producer

import ir.erfansn.artouch.common.util.Point

data class TouchEvent(
    val pressed: Boolean,
    val position: Point,
) {
    companion object {
        val RELEASE = TouchEvent(false, Point(0f, 0f))
    }
}
