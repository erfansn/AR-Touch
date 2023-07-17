#include <jni.h>
#include <string>
#include <android/log.h>

#define LogD(...)  __android_log_print(ANDROID_LOG_DEBUG, "UtilsNative", __VA_ARGS__)

// TODO: Use google's libyuv to better performance
extern "C"
JNIEXPORT void JNICALL
Java_ir_erfansn_artouch_producer_detector_util_DefaultImageRotationHelper_rotate(
        JNIEnv *env, jobject thiz,
        jobject inputBuffer,
        jint rowStride,
        jint rotationDegrees,
        jobject outputBuffer
) {
    auto *inputData = static_cast<jbyte *>(env->GetDirectBufferAddress(inputBuffer));
    auto *outputData = static_cast<jbyte *>(env->GetDirectBufferAddress(outputBuffer));

    auto column = env->GetDirectBufferCapacity(inputBuffer) / rowStride;
    switch (rotationDegrees) {
        case 90:
            for (auto i = 0; i < column; ++i) {
                for (auto j = 1; j < rowStride; ++j) {
                    const auto srcIndex = i * rowStride + j;
                    const auto dstIndex = j * column + (column - i - 1);
                    outputData[dstIndex] = inputData[srcIndex];
                }
            }
            break;

        case 180:
            for (auto i = 0; i < column; ++i) {
                for (auto j = 0; j < rowStride; ++j) {
                    const auto srcIndex = i * rowStride + j;
                    const auto dstIndex = (column - i - 1) * rowStride + (rowStride - j - 1);
                    outputData[dstIndex] = inputData[srcIndex];
                }
            }
            break;

        case 270:
            for (auto i = 0; i < column; ++i) {
                for (auto j = 0; j < rowStride; ++j) {
                    const auto srcIndex = i * rowStride + j;
                    const auto dstIndex = (rowStride - j - 1) * column + i;
                    outputData[dstIndex] = inputData[srcIndex];
                }
            }
            break;

        default:
            // No rotation, just copy the input to the output
            std::memcpy(outputData, inputData, rowStride * column);
            break;
    }
}
