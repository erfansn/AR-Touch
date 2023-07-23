package ir.erfansn.artouch.di

import ir.erfansn.artouch.common.util.Point
import ir.erfansn.artouch.producer.extractor.TouchPositionExtractor

object DummyTouchPositionExtractor : TouchPositionExtractor {
    override fun extract(target: Point, boundary: Array<Point>): Point {
        TODO("Not yet implemented")
    }
}
