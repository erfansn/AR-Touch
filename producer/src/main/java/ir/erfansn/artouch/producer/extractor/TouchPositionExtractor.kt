package ir.erfansn.artouch.producer.extractor

import android.graphics.PointF

interface TouchPositionExtractor {
    fun extract(
        target: PointF,
        boundary: Array<PointF>,
    ): PointF
}
