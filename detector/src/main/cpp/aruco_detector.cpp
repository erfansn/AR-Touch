#include <jni.h>
#include <opencv2/core/mat.hpp>
#include "aruco_nano.h"
#include <android/log.h>
#include <omp.h>

#define LogD(...)  __android_log_print(ANDROID_LOG_DEBUG, "ArucoDetectorNative", __VA_ARGS__)

extern "C" {

JNIEXPORT jobjectArray JNICALL
Java_ir_erfansn_artouch_detector_marker_ArUcoMarkerDetector_detectArUco(
        JNIEnv *env, jobject thiz,
        jint width,
        jint height,
        jobject frameBuffer
) {
    auto pFrameBuffer = env->GetDirectBufferAddress(frameBuffer);

    cv::Mat grayFrame(height, width, CV_8UC1, pFrameBuffer);
    auto markers = aruconano::MarkerDetector::detect(grayFrame);

    const int MARKER_COUNT = 4;

    static float corners[MARKER_COUNT][2];
    static int tryToDetection = 0;

    const int MAX_TRY_TO_DETECTION = 30;

    if (!markers.empty()) {
        tryToDetection = 0;
        for (auto & marker: markers) {
            if (marker.id < MARKER_COUNT) {
                corners[marker.id][0] = marker[0].x;
                corners[marker.id][1] = marker[0].y;
            }
        }
    } else if (++tryToDetection == MAX_TRY_TO_DETECTION) {
        tryToDetection = 0;
        for (auto &corner: corners) {
            corner[0] = 0;
            corner[1] = 0;
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
        env->DeleteLocalRef(point);
    }
    return points;
}

JNIEXPORT void JNICALL
Java_ir_erfansn_artouch_detector_marker_ArUcoMarkerDetector_rotateYuvImage(
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
            for (int row = 0; row < inputHeight; ++row) {
                for (int col = 0; col < inputWidth; ++col) {
                    const int srcIndex = row * inputWidth + col;
                    const int dstIndex = col * inputHeight + (inputHeight - row - 1);
                    outputData[dstIndex] = inputData[srcIndex];
                }
            }
            break;

        case 180:
            for (int row = 0; row < inputHeight; ++row) {
                for (int col = 0; col < inputWidth; ++col) {
                    const int srcIndex = row * inputWidth + col;
                    const int dstIndex = (inputHeight - row - 1) * inputWidth + (inputWidth - col - 1);
                    outputData[dstIndex] = inputData[srcIndex];
                }
            }
            break;

        case 270:
            for (int row = 0; row < inputHeight; ++row) {
                for (int col = 0; col < inputWidth; ++col) {
                    const int srcIndex = row * inputWidth + col;
                    const int dstIndex = (inputWidth - col - 1) * inputHeight + row;
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
