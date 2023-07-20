#include <jni.h>
#include <opencv2/core/mat.hpp>
#include <android/log.h>

#include "aruco_nano.h"

#define LogD(...)  __android_log_print(ANDROID_LOG_DEBUG, "ArucoDetectorNative", __VA_ARGS__)

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_ir_erfansn_artouch_producer_detector_aruco_ArUcoMarkerDetector_detectArUco(
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

    auto pointClass = env->FindClass("ir/erfansn/artouch/common/util/Point");
    auto points = env->NewObjectArray(MARKER_COUNT, pointClass, nullptr);
    for (auto i = 0; i < MARKER_COUNT; i++) {
        auto pointFConstructor = env->GetMethodID(pointClass, "<init>", "(FF)V");
        auto point = env->NewObject(pointClass, pointFConstructor, corners[i][0], corners[i][1]);
        env->SetObjectArrayElement(points, i, point);
    }
    return points;
}
