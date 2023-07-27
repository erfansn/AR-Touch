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
import java.nio.ByteBuffer

class StubImageRotationHelper : ImageRotationHelper {

    override fun Bitmap.rotate(degrees: Int): Bitmap {
        return this
    }

    override fun ByteBuffer.rotate(rowStride: Int, degrees: Int): Pair<ByteBuffer, Int> {
        return this to rowStride
    }
}
