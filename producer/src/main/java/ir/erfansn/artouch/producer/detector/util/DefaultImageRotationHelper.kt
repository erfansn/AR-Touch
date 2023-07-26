/*
 * Copyright (C) 2023 ErfanSn
 *
 * AR Touch is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AR Touch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AR Touch.  If not, see <https://www.gnu.org/licenses/>.
 */

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
