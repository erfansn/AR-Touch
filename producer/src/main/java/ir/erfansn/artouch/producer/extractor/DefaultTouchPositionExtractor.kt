package ir.erfansn.artouch.producer.extractor

import android.graphics.PointF

internal class DefaultTouchPositionExtractor : TouchPositionExtractor {

    init {
        System.loadLibrary("touch_position_extractor")
    }

    override fun extract(
        target: PointF,
        boundary: Array<PointF>,
    ): PointF {
        require(boundary.size == 4)

        return extractTouchPosition(target, boundary)
    }

    private external fun extractTouchPosition(
        target: PointF,
        boundary: Array<PointF>,
    ): PointF
}
