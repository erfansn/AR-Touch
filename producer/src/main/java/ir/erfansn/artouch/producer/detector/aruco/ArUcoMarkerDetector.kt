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
import kotlin.time.measureTime

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

        val adjustedImageSize: Size
        val markersPosition: Array<Point>
        val inferenceTime = measureTime {
            val yBuffer = imageProxy.planes[0].buffer

            val (rotatedBuffer, outputRowStride) = with(imageRotationHelper) {
                yBuffer.rotate(
                    rowStride = imageProxy.width,
                    degrees = imageProxy.imageInfo.rotationDegrees,
                )
            }

            val column = rotatedBuffer.capacity() / outputRowStride
            adjustedImageSize = Size(
                width = outputRowStride,
                height = column,
            )
            markersPosition = detectArUco(
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
