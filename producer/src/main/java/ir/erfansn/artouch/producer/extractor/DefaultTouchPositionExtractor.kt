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
