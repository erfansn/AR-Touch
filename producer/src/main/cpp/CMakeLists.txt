cmake_minimum_required(VERSION 3.22.1)

project(ar_touch)

add_library(
        aruco_detector
        SHARED
        aruco/aruco_detector.cpp)

add_library(
        touch_position_extractor
        SHARED
        touch_position_extractor.cpp)

add_library(
        utils
        SHARED
        utils.cpp)

find_package(OpenCV 4.10 REQUIRED core imgproc highgui calib3d)

target_link_libraries(
        aruco_detector
        log
        ${OpenCV_LIBRARIES})

target_link_libraries(
        touch_position_extractor
        log
        ${OpenCV_LIBRARIES})

target_link_libraries(
        utils
        log)
