package ir.erfansn.artouch.detector.marker

import android.graphics.ImageFormat
import android.graphics.PointF
import android.util.Log
import android.util.Size
import androidx.camera.core.ImageProxy
import ir.erfansn.artouch.detector.ObjectDetector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.nio.ByteBuffer
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class ArUcoMarkerDetector : ObjectDetector<MarkerDetectionResult> {

    init {
        System.loadLibrary("aruco_detector")
    }

    private val _result = MutableSharedFlow<MarkerDetectionResult>(extraBufferCapacity = 1)
    override val result = _result.asSharedFlow()

    override fun detect(imageProxy: ImageProxy) {
        require(imageProxy.format == ImageFormat.YUV_420_888) { "Image format must be YUV 420 888." }

        _result.tryEmit(
            detectArUco(imageProxy).let { (inferenceTime, markers) ->
                val adjustedImageSize =
                    if (imageProxy.imageInfo.rotationDegrees % 180 == 0) {
                        Size(imageProxy.width, imageProxy.height)
                    } else {
                        Size(imageProxy.height, imageProxy.width)
                    }

                MarkerDetectionResult(
                    inferenceTime = inferenceTime,
                    inputImageSize = adjustedImageSize,
                    markers = markers / adjustedImageSize
                )
            }.also {
                Log.v(TAG, it.toString())
            }
        )
        imageProxy.close()
    }

    @OptIn(ExperimentalTime::class)
    private fun detectArUco(imageProxy: ImageProxy): Pair<Long, Array<PointF>> {
        val markers: Array<PointF>
        val takenTime = measureTime {
            val inputWidth = imageProxy.width
            val inputHeight = imageProxy.height

            val (outputWidth, outputHeight, rotatedImageBuffer) = rotateYuvImage(
                width = inputWidth,
                height = inputHeight,
                rotationDegrees = imageProxy.imageInfo.rotationDegrees,
                inputBuffer = imageProxy.planes[0].buffer,
            )
            markers = detectArUco(
                width = outputWidth,
                height = outputHeight,
                frameBuffer = rotatedImageBuffer,
            )
        }
        return takenTime.inWholeMilliseconds to markers
    }

    private fun rotateYuvImage(
        width: Int,
        height: Int,
        rotationDegrees: Int,
        inputBuffer: ByteBuffer,
    ): Triple<Int, Int, ByteBuffer> {
        require(rotationDegrees in listOf(0, 90, 180, 270))

        val (outputWidth, outputHeight) = when (rotationDegrees) {
            90, 270 -> height to width
            else -> width to height
        }
        val rotatedImageBuffer = ByteBuffer.allocateDirect(outputWidth * outputHeight)

        rotateYuvImage(
            width = width,
            height = height,
            rotationDegrees = rotationDegrees,
            inputBuffer = inputBuffer,
            outputBuffer = rotatedImageBuffer,
        )
        return Triple(outputWidth, outputHeight, rotatedImageBuffer)
    }

    private external fun rotateYuvImage(
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
    ): Array<PointF>

    companion object {
        private const val TAG = "ArUcoMarkerDetector"
    }
}

private operator fun Array<PointF>.div(size: Size): Array<PointF> {
    return map { PointF(it.x / size.width, it.y / size.height) }.toTypedArray()
}
