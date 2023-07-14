package ir.erfansn.artouch.producer.extractor

import ir.erfansn.artouch.common.util.Point

interface TouchPositionExtractor {
    fun extract(
        target: Point,
        boundary: Array<Point>,
    ): Point
}
