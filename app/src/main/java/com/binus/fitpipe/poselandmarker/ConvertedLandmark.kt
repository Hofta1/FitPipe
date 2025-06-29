package com.binus.fitpipe.poselandmarker

import com.google.gson.annotations.SerializedName

data class ConvertedLandmark(
    val x: Float,
    val y: Float,
    val z: Float,
)

data class ConvertedLandmarkList(
    @SerializedName("posture")
    val poseKey: String,
    @SerializedName("mediapipe")
    val landmarks: List<Float>,
)
