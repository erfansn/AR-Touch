package ir.erfansn.artouch.producer.detector.hand

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import kotlin.math.max

class HandLandmarksView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var pointPaint = Paint()
    private var linePaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        initPaints()
    }

    private fun initPaints() {
        linePaint.color = Color.LTGRAY
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        result?.let { result ->
            for (landmarks in result.landmarks) {
                for (connection in HandLandmarker.HAND_CONNECTIONS) {
                    val startPreviewOptimizedLandmark = landmarks[connection.start()].previewOptimized
                    val endPreviewOptimizedLandmark = landmarks[connection.end()].previewOptimized
                    canvas.drawLine(
                        startPreviewOptimizedLandmark.x(),
                        startPreviewOptimizedLandmark.y(),
                        endPreviewOptimizedLandmark.x(),
                        endPreviewOptimizedLandmark.y(),
                        linePaint
                    )
                }
                for (normalizedLandmark in landmarks) {
                    val previewOptimizedLandmark = normalizedLandmark.previewOptimized
                    canvas.drawPoint(
                        previewOptimizedLandmark.x(),
                        previewOptimizedLandmark.y(),
                        pointPaint
                    )
                }
            }
        }
    }

    private val NormalizedLandmark.previewOptimized: NormalizedLandmark
        get() {
            val widthSizeDeltaHalf = (imageWidth * scaleFactor - width).toInt() / 2
            val heightSizeDeltaHalf = (imageHeight * scaleFactor - height).toInt() / 2
            return NormalizedLandmark.create(
                x() * imageWidth * scaleFactor - widthSizeDeltaHalf,
                y() * imageHeight * scaleFactor - heightSizeDeltaHalf,
                z()
            )
        }

    var result: HandDetectionResult? = null
        set(value) {
            field = value
            value ?: return

            imageWidth = value.inputImageSize.width
            imageHeight = value.inputImageSize.height

            scaleFactor = max(width / imageWidth.toFloat(), height / imageHeight.toFloat())

            invalidate()
        }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 8F
    }
}
