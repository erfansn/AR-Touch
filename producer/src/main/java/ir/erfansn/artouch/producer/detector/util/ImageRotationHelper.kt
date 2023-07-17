package ir.erfansn.artouch.producer.detector.util

import android.graphics.Bitmap
import java.nio.ByteBuffer

interface ImageRotationHelper {
    fun Bitmap.rotate(degrees: Int): Bitmap
    fun ByteBuffer.rotate(rowStride: Int, degrees: Int): Pair<ByteBuffer, Int>
}
