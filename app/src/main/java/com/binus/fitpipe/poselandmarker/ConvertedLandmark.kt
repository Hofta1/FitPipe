package com.binus.fitpipe.poselandmarker

data class ConvertedLandmark(
    val x: Float,
    val y: Float,
    val z: Float,
)

data class ConvertedLandmarkList(
    val poseKey: String,
    val landmarks: List<Float>,
)
