package ir.erfansn.artouch.producer.detector.marker

import android.graphics.PointF
import android.util.Size

data class MarkersDetectionResult(
    val inferenceTime: Long = -1,
    val inputImageSize: Size = Size(-1, -1),
    val positions: Array<PointF> = emptyArray(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MarkersDetectionResult

        if (inferenceTime != other.inferenceTime) return false
        if (inputImageSize != other.inputImageSize) return false
        if (!positions.contentEquals(other.positions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = inferenceTime.hashCode()
        result = 31 * result + inputImageSize.hashCode()
        result = 31 * result + positions.contentHashCode()
        return result
    }
}
