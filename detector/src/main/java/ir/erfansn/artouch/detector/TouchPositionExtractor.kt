package ir.erfansn.artouch.detector

import android.graphics.PointF
import kotlinx.coroutines.flow.Flow

interface TouchPositionExtractor {
    val touchPosition: Flow<PointF>
}
