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

@file:OptIn(ExperimentalStdlibApi::class)

package ir.erfansn.artouch.benchmark.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.graphics.Rect
import androidx.camera.core.ImageInfo
import androidx.camera.core.ImageProxy
import androidx.camera.core.impl.TagBundle
import androidx.camera.core.impl.utils.ExifData
import java.nio.ByteBuffer

fun Context.loadJpegImageIntoImageProxy(
    filePath: String,
    format: Int = ImageFormat.YUV_420_888,
): ImageProxy {
    require(filePath.endsWith(".jpg"))
    val bitmap = assets.open(filePath).use { BitmapFactory.decodeStream(it) }

    return object : ImageProxy {
        override fun getWidth() = bitmap.width

        override fun getHeight() = bitmap.height

        override fun getImage() = null

        override fun getFormat() = format

        override fun getCropRect() = Rect(0, 0, width, height)

        override fun setCropRect(rect: Rect?) = Unit

        override fun getPlanes(): Array<out ImageProxy.PlaneProxy> {
            return when (getFormat()) {
                ImageFormat.YUV_420_888 -> bitmap.toYuvPlanes()
                PixelFormat.RGBA_8888 -> bitmap.toRgbaPlanes()
                else -> throw IllegalArgumentException()
            }
        }

        override fun getImageInfo() = object : ImageInfo {
            override fun getTagBundle() = TagBundle.emptyBundle()

            override fun getTimestamp() = System.currentTimeMillis()

            override fun getRotationDegrees() = 0

            override fun populateExifData(p0: ExifData.Builder) = Unit
        }

        override fun close() = Unit
    }
}

fun Context.loadJpegImageAsBitmap(filePath: String): Bitmap {
    require(filePath.endsWith(".jpg"))
    return assets.open(filePath).use { BitmapFactory.decodeStream(it) }
}

fun Bitmap.toYuvPlanes(): Array<ImageProxy.PlaneProxy> {
    val (yBuffer, uvBuffer) = convertToYuv420()
    val yPlane = object : ImageProxy.PlaneProxy {
        override fun getBuffer() = yBuffer

        override fun getPixelStride() = 1

        override fun getRowStride() = width
    }
    val uvPlane = object : ImageProxy.PlaneProxy {
        override fun getBuffer() = uvBuffer

        override fun getPixelStride() = 2

        override fun getRowStride() = width / 2
    }
    return arrayOf(yPlane, uvPlane, uvPlane)
}

fun Bitmap.toRgbaPlanes(): Array<ImageProxy.PlaneProxy> {
    val (rgbaBuffer) = convertToRgb8888()
    val rgbaPlane = object : ImageProxy.PlaneProxy {
        override fun getBuffer() = rgbaBuffer

        override fun getPixelStride() = 4

        override fun getRowStride() = width * 4
    }
    return arrayOf(rgbaPlane)
}

private fun Bitmap.convertToYuv420(): Pair<ByteBuffer, ByteBuffer> {
    val yBuffer = ByteBuffer.allocateDirect(width * height)
    val uvBuffer = ByteBuffer.allocateDirect((width * height) / 2)

    val rgbaBuffer = ByteBuffer.allocateDirect(byteCount)
    copyPixelsToBuffer(rgbaBuffer)
    rgbaBuffer.rewind()

    for (y in 0..<height) {
        for (x in 0..<width) {
            val (r, g, b) = rgbaBuffer.int.let {
                Triple(
                    it and 0xFF0000 shr 16,
                    it and 0x00FF00 shr 8,
                    it and 0x0000FF shr 0,
                )
            }

            val yValue = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            yBuffer.put(yValue.toByte())

            if (y % 2 == 0 && x % 2 == 0) {
                val uValue = (-0.169 * r - 0.331 * g + 0.5 * b + 128).toInt()
                val vValue = (0.5 * r - 0.419 * g - 0.081 * b + 128).toInt()

                uvBuffer.put(uValue.toByte())
                uvBuffer.put(vValue.toByte())
            }
        }
    }
    return yBuffer to uvBuffer
}

private fun Bitmap.convertToRgb8888(): Array<ByteBuffer> {
    val rgbaBuffer = ByteBuffer.allocateDirect(byteCount)
    copyPixelsToBuffer(rgbaBuffer)
    rgbaBuffer.rewind()
    return arrayOf(rgbaBuffer)
}
