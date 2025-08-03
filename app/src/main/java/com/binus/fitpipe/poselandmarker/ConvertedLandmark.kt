package com.binus.fitpipe.poselandmarker

import com.google.gson.annotations.SerializedName
import java.util.Optional

data class ConvertedLandmark(
    val keyPoint: MediaPipeKeyPoint? = null,
    val x: Float,
    val y: Float,
    val z: Float,
    val visibility: Optional<Float>,
    val presence: Optional<Float>,
)

data class ConvertedLandmarkList(
    @SerializedName("posture")
    val poseKey: String,
    @SerializedName("mediapipe")
    val landmarks: List<Float>,
)

fun ConvertedLandmark.addKeyPoint(id: Int): ConvertedLandmark {
    val keyPoint = mediaPipeKeyPoints.find { it.keyId == id }
    return this.copy(keyPoint = keyPoint, x = this.x, y = this.y, z = this.z, visibility = this.visibility, presence = this.presence)
}

data class MediaPipeKeyPoint(
    val keyId: Int,
    val keyName: String,
)

// to debug
val mediaPipeKeyPoints =
    listOf(
        MediaPipeKeyPoint(0, "NOSE"),
        MediaPipeKeyPoint(1, "LEFT_EYE_INNER"),
        MediaPipeKeyPoint(2, "LEFT_EYE"),
        MediaPipeKeyPoint(3, "LEFT_EYE_OUTER"),
        MediaPipeKeyPoint(4, "RIGHT_EYE_INNER"),
        MediaPipeKeyPoint(5, "RIGHT_EYE"),
        MediaPipeKeyPoint(6, "RIGHT_EYE_OUTER"),
        MediaPipeKeyPoint(7, "LEFT_EAR"),
        MediaPipeKeyPoint(8, "RIGHT_EAR"),
        MediaPipeKeyPoint(9, "LEFT_MOUTH"),
        MediaPipeKeyPoint(10, "RIGHT_MOUTH"),
        MediaPipeKeyPoint(11, "LEFT_SHOULDER"),
        MediaPipeKeyPoint(12, "RIGHT_SHOULDER"),
        MediaPipeKeyPoint(13, "LEFT_ELBOW"),
        MediaPipeKeyPoint(14, "RIGHT_ELBOW"),
        MediaPipeKeyPoint(15, "LEFT_WRIST"),
        MediaPipeKeyPoint(16, "RIGHT_WRIST"),
        MediaPipeKeyPoint(17, "LEFT_PINKY"),
        MediaPipeKeyPoint(18, "RIGHT_PINKY"),
        MediaPipeKeyPoint(19, "LEFT_INDEX"),
        MediaPipeKeyPoint(20, "RIGHT_INDEX"),
        MediaPipeKeyPoint(21, "LEFT_THUMB"),
        MediaPipeKeyPoint(22, "RIGHT_THUMB"),
        MediaPipeKeyPoint(23, "LEFT_HIP"),
        MediaPipeKeyPoint(24, "RIGHT_HIP"),
        MediaPipeKeyPoint(25, "LEFT_KNEE"),
        MediaPipeKeyPoint(26, "RIGHT_KNEE"),
        MediaPipeKeyPoint(27, "LEFT_ANKLE"),
        MediaPipeKeyPoint(28, "RIGHT_ANKLE"),
        MediaPipeKeyPoint(29, "LEFT_HEEL"),
        MediaPipeKeyPoint(30, "RIGHT_HEEL"),
        MediaPipeKeyPoint(31, "LEFT_FOOT_INDEX"),
        MediaPipeKeyPoint(32, "RIGHT_FOOT_INDEX"),
    )
