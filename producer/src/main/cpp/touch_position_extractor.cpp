#include <jni.h>
#include <opencv2/core/mat.hpp>
#include <opencv2/imgproc.hpp>
#include <android/log.h>

#define LogD(...)  __android_log_print(ANDROID_LOG_DEBUG, "TouchPositionExtractorNative", __VA_ARGS__)

using namespace cv;

extern "C"
JNIEXPORT jobject JNICALL
Java_ir_erfansn_artouch_producer_extractor_DefaultTouchPositionExtractor_extractTouchPosition(
        JNIEnv *env, jobject thiz,
        jobject target,
        jobjectArray boundary
) {
    auto pointClass = env->FindClass("ir/erfansn/artouch/common/util/Point");
    auto xFieldId = env->GetFieldID(pointClass, "x", "F");
    auto yFieldId = env->GetFieldID(pointClass, "y", "F");

    const auto POINT_COUNT = 4;
    Point2f srcPoints[POINT_COUNT];
    for (auto i = 0; i < POINT_COUNT; i++) {
        auto point = env->GetObjectArrayElement(boundary, i);
        srcPoints[i].x = env->GetFloatField(point, xFieldId);
        srcPoints[i].y = env->GetFloatField(point, yFieldId);
    }
    auto dstPoints = new Point2f[POINT_COUNT] {
        Point2f(0, 0),
        Point2f(1, 0),
        Point2f(1, 1),
        Point2f(0, 1),
    };
    auto transformedMat = getPerspectiveTransform(
        srcPoints,
        dstPoints
    );

    auto targetX = env->GetFloatField(target, xFieldId);
    auto targetY = env->GetFloatField(target, yFieldId);

    // https://stackoverflow.com/questions/57399915/how-do-i-determine-the-locations-of-the-points-after-perspective-transform-in-t
    auto finalX = (
        transformedMat.at<double>(0, 0) * targetX +
        transformedMat.at<double>(0, 1) * targetY +
        transformedMat.at<double>(0, 2)) / (
        transformedMat.at<double>(2, 0) * targetX +
        transformedMat.at<double>(2, 1) * targetY +
        transformedMat.at<double>(2, 2)
    );
    auto finalY = (
        transformedMat.at<double>(1, 0) * targetX +
        transformedMat.at<double>(1, 1) * targetY +
        transformedMat.at<double>(1, 2)) / (
        transformedMat.at<double>(2, 0) * targetX +
        transformedMat.at<double>(2, 1) * targetY +
        transformedMat.at<double>(2, 2)
    );
    if (finalX < 0.0) {
        finalX = 0.0;
    } else if (finalX > 1.0) {
        finalX = 1.0;
    }
    if (finalY < 0.0) {
        finalY = 0.0;
    } else if (finalY > 1.0) {
        finalY = 1.0;
    }
    LogD("Final touch point is (%lf, %lf)", finalX, finalY);

    auto pointFConstructor = env->GetMethodID(pointClass, "<init>", "(FF)V");
    auto touchPosition = env->NewObject(pointClass, pointFConstructor, (float) finalX, (float) finalY);
    return touchPosition;
}
