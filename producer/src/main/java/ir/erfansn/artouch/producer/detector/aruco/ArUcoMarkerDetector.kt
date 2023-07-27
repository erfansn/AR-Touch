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

import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageProxy
import ir.erfansn.artouch.producer.detector.ObjectDetector
import ir.erfansn.artouch.common.util.Point
import ir.erfansn.artouch.common.util.Size
import ir.erfansn.artouch.producer.detector.util.ImageRotationHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.nio.ByteBuffer
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

internal class ArUcoMarkerDetector(
    private val imageRotationHelper: ImageRotationHelper,
) : ObjectDetector<ArUcoDetectionResult> {

    init {
        System.loadLibrary("aruco_detector")
    }

    private val _result = MutableSharedFlow<ArUcoDetectionResult>(extraBufferCapacity = 1)
    override val result = _result.asSharedFlow()

    @OptIn(ExperimentalTime::class)
    override fun detect(imageProxy: ImageProxy) {
        require(imageProxy.format == ImageFormat.YUV_420_888) { "Image format must be YUV 420 888." }

        val yBuffer = imageProxy.planes[0].buffer
        val (rotatedBuffer, outputRowStride) = with(imageRotationHelper) {
            yBuffer.rotate(
                rowStride = imageProxy.width,
                degrees = imageProxy.imageInfo.rotationDegrees,
            )
        }

        val column = rotatedBuffer.capacity() / outputRowStride
        val adjustedImageSize = Size(
            width = outputRowStride,
            height = column,
        )
        val (markersPosition, inferenceTime) = measureTimedValue {
            detectArUco(
                width = outputRowStride,
                height = column,
                frameBuffer = rotatedBuffer,
            )
        }

        ArUcoDetectionResult(
            inferenceTime = inferenceTime.inWholeMilliseconds,
            inputImageSize = adjustedImageSize,
            positions = if (markersPosition.any { it == Point(-1f, -1f) }) {
                emptyArray()
            } else {
                markersPosition / adjustedImageSize
            }
        ).also {
            Log.v(TAG, it.toString())
        }.run(_result::tryEmit)

        imageProxy.close()
    }

    private external fun detectArUco(
        width: Int,
        height: Int,
        frameBuffer: ByteBuffer,
    ): Array<Point>

    companion object {
        private const val TAG = "ArUcoMarkerDetector"
    }
}

private operator fun Array<Point>.div(size: Size): Array<Point> {
    return map { Point(it.x / size.width, it.y / size.height) }.toTypedArray()
}
