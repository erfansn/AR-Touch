package ir.erfansn.artouch.handdetection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

class MediaPipeHandDetector(
    context: Context,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : HandDetector {

    private var handLandmarker: HandLandmarker? = null

    override val result = callbackFlow {
        val baseOptions = BaseOptions.builder()
            .setDelegate(Delegate.GPU)
            .setModelAssetPath("hand_landmarker.task")
            .build()

        val options =
            HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setNumHands(1)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { result, input ->
                    val finishTimeMs = SystemClock.uptimeMillis()
                    val inferenceTime = finishTimeMs - result.timestampMs()

                    trySend(
                        HandDetectionResult(
                            result,
                            inferenceTime,
                            input.height,
                            input.width
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
        }
    }.flowOn(defaultDispatcher)

    override fun detect(imageProxy: ImageProxy) {
        val frameTime = SystemClock.uptimeMillis()

        // Copy out RGB bits from the frame to a bitmap buffer
        val bitmapBuffer =
            Bitmap.createBitmap(
                imageProxy.width,
                imageProxy.height,
                Bitmap.Config.ARGB_8888
            )
        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
        imageProxy.close()

        val matrix = Matrix().apply {
            // Rotate the frame received from the camera to be in the same direction as it'll be shown
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
        }
        val rotatedBitmap = Bitmap.createBitmap(
            bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
            matrix, true
        )

        // Convert the input Bitmap object to an MPImage object to run inference
        val mpImage = BitmapImageBuilder(rotatedBitmap).build()

        try {
            handLandmarker?.detectAsync(mpImage, frameTime)
        } catch (e: Exception) {
            Log.e(TAG, "Try detect Hand Landmarker when it was closed!")
        }
    }

    companion object {
        private const val TAG = "MediaPipeHandDetector"
    }
}