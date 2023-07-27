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
