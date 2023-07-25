package ir.erfansn.artouch.producer.extractor

import ir.erfansn.artouch.common.util.Point

object StubTouchPositionExtractor : TouchPositionExtractor {
    override fun extract(target: Point, boundary: Array<Point>) =
        Point(-1f, -1f)
}
