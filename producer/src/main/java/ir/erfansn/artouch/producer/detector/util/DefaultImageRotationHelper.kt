package ir.erfansn.artouch.producer.detector.util

import android.graphics.Bitmap
import android.graphics.Matrix
import java.nio.ByteBuffer

internal class DefaultImageRotationHelper : ImageRotationHelper {

    init {
        System.loadLibrary("utils")
    }

    override fun Bitmap.rotate(degrees: Int): Bitmap {
        val matrix = Matrix().apply {
            // Rotate the frame received from the camera to be in the same direction as it'll be shown
            postRotate(degrees.toFloat())
        }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    override fun ByteBuffer.rotate(rowStride: Int, degrees: Int): Pair<ByteBuffer, Int> {
        val outputRowStride = when (degrees) {
            90, 270 -> capacity() / rowStride
            0, 180 -> rowStride
            else -> throw IllegalArgumentException()
        }

        val rotatedImageBuffer = ByteBuffer.allocateDirect(capacity())
        rotate(
            rowStride = rowStride,
            rotationDegrees = degrees,
            outputBuffer = rotatedImageBuffer,
        )
        return Pair(rotatedImageBuffer, outputRowStride)
    }

    private external fun ByteBuffer.rotate(
        rowStride: Int,
        rotationDegrees: Int,
        outputBuffer: ByteBuffer,
    )
}
