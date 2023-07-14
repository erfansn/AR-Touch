package ir.erfansn.artouch.producer.detector.marker

import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageProxy
import ir.erfansn.artouch.producer.detector.ObjectDetector
import ir.erfansn.artouch.common.util.Point
import ir.erfansn.artouch.common.util.Size
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.nio.ByteBuffer
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

internal class ArUcoMarkerDetector : ObjectDetector<MarkersDetectionResult> {

    init {
        System.loadLibrary("aruco_detector")
    }

    private val _result = MutableSharedFlow<MarkersDetectionResult>(extraBufferCapacity = 1)
    override val result = _result.asSharedFlow()

    @OptIn(ExperimentalTime::class)
    override fun detect(imageProxy: ImageProxy) {
        require(imageProxy.format == ImageFormat.YUV_420_888) { "Image format must be YUV 420 888." }

        val adjustedImageSize: Size
        val markersPosition: Array<Point>
        val inferenceTime = measureTime {
            val (outputWidth, outputHeight, rotatedImageBuffer) = rotateYImage(
                width = imageProxy.width,
                height = imageProxy.height,
                rotationDegrees = imageProxy.imageInfo.rotationDegrees,
                inputBuffer = imageProxy.planes[0].buffer,
            )

            adjustedImageSize = Size(outputWidth, outputHeight)
            markersPosition = detectArUco(
                width = outputWidth,
                height = outputHeight,
                frameBuffer = rotatedImageBuffer,
            )
        }

        MarkersDetectionResult(
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

    private fun rotateYImage(
        width: Int,
        height: Int,
        rotationDegrees: Int,
        inputBuffer: ByteBuffer,
    ): Triple<Int, Int, ByteBuffer> {
        val (outputWidth, outputHeight) = when (rotationDegrees) {
            90, 270 -> height to width
            0, 180 -> width to height
            else -> throw IllegalStateException()
        }
        val rotatedImageBuffer = ByteBuffer.allocateDirect(outputWidth * outputHeight)

        rotateYImage(
            width = width,
            height = height,
            rotationDegrees = rotationDegrees,
            inputBuffer = inputBuffer,
            outputBuffer = rotatedImageBuffer,
        )
        return Triple(outputWidth, outputHeight, rotatedImageBuffer)
    }

    private external fun rotateYImage(
        width: Int,
        height: Int,
        rotationDegrees: Int,
        inputBuffer: ByteBuffer,
        outputBuffer: ByteBuffer,
    )

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
