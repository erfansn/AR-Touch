package ir.erfansn.artouch.benchmark.di

import android.graphics.Bitmap
import ir.erfansn.artouch.producer.detector.util.ImageRotationHelper
import java.nio.ByteBuffer

class StubImageRotationHelper : ImageRotationHelper {

    override fun Bitmap.rotate(degrees: Int): Bitmap {
        return this
    }

    override fun ByteBuffer.rotate(rowStride: Int, degrees: Int): Pair<ByteBuffer, Int> {
        return this to rowStride
    }
}
