package ir.erfansn.artouch.producer.detector.aruco

import ir.erfansn.artouch.common.util.Point
import ir.erfansn.artouch.common.util.Size

data class ArUcoDetectionResult(
    val inferenceTime: Long,
    val inputImageSize: Size,
    val positions: Array<Point>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArUcoDetectionResult

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
