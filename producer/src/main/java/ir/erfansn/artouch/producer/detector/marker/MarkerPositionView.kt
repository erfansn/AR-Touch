package ir.erfansn.artouch.producer.detector.marker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.max

class MarkerPositionView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val pointPaint = Paint()
    private val linePaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        pointPaint.color = Color.RED
        pointPaint.strokeWidth = 12f
        pointPaint.style = Paint.Style.FILL

        linePaint.color = Color.GREEN
        linePaint.isAntiAlias = true
        linePaint.strokeWidth = 6f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        result?.let {
            val nonZeroMarkers = it.markers.filterNot { it.x == 0f || it.y == 0f }
            if (nonZeroMarkers.isEmpty()) return

            for (i in 1..nonZeroMarkers.size) {
                val startPoint = nonZeroMarkers[i - 1]
                val endPoint = nonZeroMarkers[i % nonZeroMarkers.size]

                val (startX, startY) = startPoint.previewOptimized
                val (endX, endY) = endPoint.previewOptimized
                canvas.drawLine(startX, startY, endX, endY, linePaint)
            }
            nonZeroMarkers.forEach { point ->
                canvas.drawPoint(point, pointPaint)
            }
        }
    }

    private fun Canvas.drawPoint(pointF: PointF, paint: Paint) {
        val (x, y) = pointF.previewOptimized
        drawPoint(x, y, paint)
    }

    private val PointF.previewOptimized: Pair<Float, Float>
        get() {
            val widthSizeHalfDelta = (imageWidth * scaleFactor - width).toInt() / 2
            val heightSizeHalfDelta = (imageHeight * scaleFactor - height).toInt() / 2
            return x * imageWidth * scaleFactor - widthSizeHalfDelta to y * imageHeight * scaleFactor - heightSizeHalfDelta
        }

    var result: MarkerDetectionResult? = null
        set(value) {
            field = value
            value ?: return

            imageWidth = value.inputImageSize.width
            imageHeight = value.inputImageSize.height
            Log.d(TAG, "Sizes image($imageWidth, $imageHeight) view($width, $height)")

            scaleFactor = max(width / imageWidth.toFloat(), height / imageHeight.toFloat())

            invalidate()
        }

    companion object {
        private const val TAG = "MarkerPositionView"
    }
}
