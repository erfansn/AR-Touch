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

package ir.erfansn.artouch.producer.detector.hand

import android.content.Context
import android.graphics.PixelFormat
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import ir.erfansn.artouch.producer.detector.ObjectDetector
import ir.erfansn.artouch.common.util.Size
import ir.erfansn.artouch.producer.detector.util.ImageRotationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

internal class HandLandmarkerDetector(
    context: Context,
    externalScope: CoroutineScope,
    private val imageRotationHelper: ImageRotationHelper,
) : ObjectDetector<HandDetectionResult> {

    private var handLandmarker: HandLandmarker? = null

    override val result = callbackFlow {
        val baseOptions = BaseOptions.builder()
            .setDelegate(Delegate.GPU)
            .setModelAssetPath("hand_landmarker.task")
            .build()

        val options =
            HandLandmarker.HandLandmarkerOptions.builder()
                .setMinHandPresenceConfidence(0.75f)
                .setMinHandDetectionConfidence(0.60f)
                .setMinTrackingConfidence(0.5f)
                .setBaseOptions(baseOptions)
                .setNumHands(1)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { result, input ->
                    val finishTimeMs = SystemClock.uptimeMillis()
                    val inferenceTime = finishTimeMs - result.timestampMs()

                    trySend(
                        HandDetectionResult(
                            inferenceTime = inferenceTime,
                            inputImageSize = Size(input.width, input.height),
                            landmarks = result.landmarks().firstOrNull() ?: emptyList(),
                        )
                    )
                }
                .setErrorListener { error ->
                    close(error)
                }
                .build()

        handLandmarker = HandLandmarker.createFromOptions(context, options)
        awaitClose {
            handLandmarker?.close()
            handLandmarker = null
            Log.i(TAG, "HandLandmerker closed")
        }
    }.shareIn(
        scope = externalScope,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    override fun detect(imageProxy: ImageProxy) {
        require(imageProxy.format == PixelFormat.RGBA_8888) { "Image format must be RGBA 8888." }

        val frameTime = SystemClock.uptimeMillis()

        // Copy out RGB bits from the frame to a bitmap buffer
        val bitmapBuffer = imageProxy.toBitmap()
        imageProxy.close()

        val rotatedBitmap = with(imageRotationHelper) {
            bitmapBuffer.rotate(degrees = imageProxy.imageInfo.rotationDegrees)
        }

        // Convert the input Bitmap object to an MPImage object to run inference
        val mpImage = BitmapImageBuilder(rotatedBitmap).build()

        try {
            handLandmarker?.detectAsync(mpImage, frameTime)
        } catch (e: Exception) {
            Log.w(TAG, "Trying to detect Hand Landmarks when detector has been closed!")
        }
    }

    companion object {
        private const val TAG = "MediaPipeHandDetector"
    }
}
