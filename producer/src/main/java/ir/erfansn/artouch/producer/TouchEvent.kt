package ir.erfansn.artouch.producer

import android.graphics.PointF

data class TouchEvent(
    val pressed: Boolean,
    val position: PointF,
) {
    companion object {
        val RELEASE = TouchEvent(false, PointF(0f, 0f))
    }
}
