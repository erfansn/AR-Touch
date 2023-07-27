/*
 * Copyright 2023 Erfan Sn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ir.erfansn.artouch.producer.detector.aruco

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import ir.erfansn.artouch.common.util.Point
import kotlin.math.max

class ArUcoMarkerView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val pointPaint = Paint()
    private val linePaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        pointPaint.color = Color.RED
        pointPaint.strokeWidth = 24f
        pointPaint.style = Paint.Style.FILL

        linePaint.color = Color.GREEN
        linePaint.strokeWidth = 12f
        pointPaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        result?.let { (_, _, positions) ->
            if (positions.isEmpty()) return

            for (i in 1..positions.size) {
                val startPoint = positions[i - 1]
                val endPoint = positions[i % positions.size]

                val (startX, startY) = startPoint.previewOptimized
                val (endX, endY) = endPoint.previewOptimized
                canvas.drawLine(startX, startY, endX, endY, linePaint)
            }
            positions.forEach { point ->
                val (x, y) = point.previewOptimized
                canvas.drawPoint(x, y, pointPaint)
            }
        }
    }

    private val Point.previewOptimized: Point
        get() {
            val widthSizeHalfDelta = (imageWidth * scaleFactor - width).toInt() / 2
            val heightSizeHalfDelta = (imageHeight * scaleFactor - height).toInt() / 2
            return Point(
                x * imageWidth * scaleFactor - widthSizeHalfDelta,
                y * imageHeight * scaleFactor - heightSizeHalfDelta
            )
        }

    var result: ArUcoDetectionResult? = null
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
        private const val TAG = "ArUcoMarkerView"
    }
}
