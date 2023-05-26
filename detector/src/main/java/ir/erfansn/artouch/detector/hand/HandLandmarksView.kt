package ir.erfansn.artouch.detector.hand

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmark
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
            val lines = mutableListOf<Float>()
            val points = mutableListOf<Float>()

            for (landmarks in result.landmarks) {
                for (i in landmarkConnections.indices step 2) {
                    val startX = landmarks[landmarkConnections[i]].previewOptimized.x()
                    val startY = landmarks[landmarkConnections[i]].previewOptimized.y()
                    val endX = landmarks[landmarkConnections[i + 1]].previewOptimized.x()
                    val endY = landmarks[landmarkConnections[i + 1]].previewOptimized.y()
                    lines.add(startX)
                    lines.add(startY)
                    lines.add(endX)
                    lines.add(endY)
                    points.add(startX)
                    points.add(startY)
                }
                canvas.drawLines(lines.toFloatArray(), linePaint)
                canvas.drawPoints(points.toFloatArray(), pointPaint)
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

        // This list defines the lines that are drawn when visualizing the hand landmark detection
        // results. These lines connect:
        // landmarkConnections[2*n] and landmarkConnections[2*n+1]
        private val landmarkConnections = listOf(
            HandLandmark.WRIST,
            HandLandmark.THUMB_CMC,
            HandLandmark.THUMB_CMC,
            HandLandmark.THUMB_MCP,
            HandLandmark.THUMB_MCP,
            HandLandmark.THUMB_IP,
            HandLandmark.THUMB_IP,
            HandLandmark.THUMB_TIP,
            HandLandmark.WRIST,
            HandLandmark.INDEX_FINGER_MCP,
            HandLandmark.INDEX_FINGER_MCP,
            HandLandmark.INDEX_FINGER_PIP,
            HandLandmark.INDEX_FINGER_PIP,
            HandLandmark.INDEX_FINGER_DIP,
            HandLandmark.INDEX_FINGER_DIP,
            HandLandmark.INDEX_FINGER_TIP,
            HandLandmark.INDEX_FINGER_MCP,
            HandLandmark.MIDDLE_FINGER_MCP,
            HandLandmark.MIDDLE_FINGER_MCP,
            HandLandmark.MIDDLE_FINGER_PIP,
            HandLandmark.MIDDLE_FINGER_PIP,
            HandLandmark.MIDDLE_FINGER_DIP,
            HandLandmark.MIDDLE_FINGER_DIP,
            HandLandmark.MIDDLE_FINGER_TIP,
            HandLandmark.MIDDLE_FINGER_MCP,
            HandLandmark.RING_FINGER_MCP,
            HandLandmark.RING_FINGER_MCP,
            HandLandmark.RING_FINGER_PIP,
            HandLandmark.RING_FINGER_PIP,
            HandLandmark.RING_FINGER_DIP,
            HandLandmark.RING_FINGER_DIP,
            HandLandmark.RING_FINGER_TIP,
            HandLandmark.RING_FINGER_MCP,
            HandLandmark.PINKY_MCP,
            HandLandmark.WRIST,
            HandLandmark.PINKY_MCP,
            HandLandmark.PINKY_MCP,
            HandLandmark.PINKY_PIP,
            HandLandmark.PINKY_PIP,
            HandLandmark.PINKY_DIP,
            HandLandmark.PINKY_DIP,
            HandLandmark.PINKY_TIP
        )
    }
}
