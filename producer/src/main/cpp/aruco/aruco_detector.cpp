#include <jni.h>
#include <opencv2/core/mat.hpp>
#include "aruco_nano.h"
#include <android/log.h>

#define LogD(...)  __android_log_print(ANDROID_LOG_DEBUG, "ArucoDetectorNative", __VA_ARGS__)

extern "C" {

JNIEXPORT jobjectArray JNICALL
Java_ir_erfansn_artouch_producer_detector_marker_ArUcoMarkerDetector_detectArUco(
        JNIEnv *env, jobject thiz,
        jint width,
        jint height,
        jobject frameBuffer
) {
    auto pFrameBuffer = env->GetDirectBufferAddress(frameBuffer);

    cv::Mat grayFrame(height, width, CV_8UC1, pFrameBuffer);
    auto markers = aruconano::MarkerDetector::detect(grayFrame);

    const auto MARKER_COUNT = 4;
    const auto MAX_TRY_TO_DETECTION = 30;

    static float corners[MARKER_COUNT][2] = {
        {-1.0f, -1.0f},
        {-1.0f, -1.0f},
        {-1.0f, -1.0f},
        {-1.0f, -1.0f},
    };
    static auto failedDetectionCounter = 0;

    if (!markers.empty()) {
        failedDetectionCounter = 0;
        for (auto & marker: markers) {
            if (marker.id < MARKER_COUNT) {
                corners[marker.id][0] = marker[0].x;
                corners[marker.id][1] = marker[0].y;
            }
        }
    } else if (++failedDetectionCounter == MAX_TRY_TO_DETECTION) {
        failedDetectionCounter = 0;
        for (auto &corner: corners) {
            corner[0] = -1.0f;
            corner[1] = -1.0f;
        }
    }
    LogD("Input image size (%d, %d)", width, height);
    LogD(
        "Markers = (%f, %f) (%f, %f) (%f, %f) (%f, %f)",
        corners[0][0],
        corners[0][1],
        corners[1][0],
        corners[1][1],
        corners[2][0],
        corners[2][1],
        corners[3][0],
        corners[3][1]
    );

    auto pointFClass = env->FindClass("android/graphics/PointF");
    auto points = env->NewObjectArray(MARKER_COUNT, pointFClass, nullptr);
    for (auto i = 0; i < MARKER_COUNT; i++) {
        auto pointFConstructor = env->GetMethodID(pointFClass, "<init>", "(FF)V");
        auto point = env->NewObject(pointFClass, pointFConstructor, corners[i][0], corners[i][1]);
        env->SetObjectArrayElement(points, i, point);
    }
    return points;
}

JNIEXPORT void JNICALL
Java_ir_erfansn_artouch_producer_detector_marker_ArUcoMarkerDetector_rotateYuvImage(
        JNIEnv* env, jobject thiz,
        jint inputWidth,
        jint inputHeight,
        jint rotationDegrees,
        jobject inputBuffer,
        jobject outputBuffer
) {
    auto* inputData = static_cast<jbyte*>(env->GetDirectBufferAddress(inputBuffer));
    auto* outputData = static_cast<jbyte*>(env->GetDirectBufferAddress(outputBuffer));

    switch (rotationDegrees) {
        case 90:
            for (auto row = 0; row < inputHeight; ++row) {
                for (auto col = 1; col < inputWidth; ++col) {
                    const auto srcIndex = row * inputWidth + col;
                    const auto dstIndex = col * inputHeight + (inputHeight - row - 1);
                    outputData[dstIndex] = inputData[srcIndex];
                }
            }
            break;

        case 180:
            for (auto row = 0; row < inputHeight; ++row) {
                for (auto col = 0; col < inputWidth; ++col) {
                    const auto srcIndex = row * inputWidth + col;
                    const auto dstIndex = (inputHeight - row - 1) * inputWidth + (inputWidth - col - 1);
                    outputData[dstIndex] = inputData[srcIndex];
                }
            }
            break;

        case 270:
            for (auto row = 0; row < inputHeight; ++row) {
                for (auto col = 0; col < inputWidth; ++col) {
                    const auto srcIndex = row * inputWidth + col;
                    const auto dstIndex = (inputWidth - col - 1) * inputHeight + row;
                    outputData[dstIndex] = inputData[srcIndex];
                }
            }
            break;

        default:
            // No rotation, just copy the input to the output
            std::memcpy(outputData, inputData, inputWidth * inputHeight);
            break;
    }
}

}
