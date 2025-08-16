package com.binus.fitpipe.poselandmarker

import com.google.gson.annotations.SerializedName
import java.util.Optional

data class ConvertedLandmark(
    val x: Float,
    val y: Float,
    val z: Float,
    val visibility: Optional<Float>,
    val presence: Optional<Float>,
    val keyPointEnum: MediaPipeKeyPointEnum? = null,
)

data class ConvertedLandmarkList(
    @SerializedName("posture")
    val poseKey: String,
    @SerializedName("mediapipe")
    val landmarks: List<Float>,
)


fun ConvertedLandmark.addKeyPointEnum(id: Int): ConvertedLandmark {
    val keyPointEnum = MediaPipeKeyPointEnum.entries.find { it.keyId == id }
    return this.copy(keyPointEnum = keyPointEnum, x = this.x, y = this.y, z = this.z, visibility = this.visibility, presence = this.presence)
}


// to debug

enum class MediaPipeKeyPointEnum(val keyId: Int, val keyName: String) {
    NOSE(0, "NOSE"),
    LEFT_EYE_INNER(1, "LEFT_EYE_INNER"),
    LEFT_EYE(2, "LEFT_EYE"),
    LEFT_EYE_OUTER(3, "LEFT_EYE_OUTER"),
    RIGHT_EYE_INNER(4, "RIGHT_EYE_INNER"),
    RIGHT_EYE(5, "RIGHT_EYE"),
    RIGHT_EYE_OUTER(6, "RIGHT_EYE_OUTER"),
    LEFT_EAR(7, "LEFT_EAR"),
    RIGHT_EAR(8, "RIGHT_EAR"),
    LEFT_MOUTH(9, "LEFT_MOUTH"),
    RIGHT_MOUTH(10, "RIGHT_MOUTH"),
    LEFT_SHOULDER(11, "LEFT_SHOULDER"),
    RIGHT_SHOULDER(12, "RIGHT_SHOULDER"),
    LEFT_ELBOW(13, "LEFT_ELBOW"),
    RIGHT_ELBOW(14, "RIGHT_ELBOW"),
    LEFT_WRIST(15, "LEFT_WRIST"),
    RIGHT_WRIST(16, "RIGHT_WRIST"),
    LEFT_PINKY(17, "LEFT_PINKY"),
    RIGHT_PINKY(18, "RIGHT_PINKY"),
    LEFT_INDEX(19, "LEFT_INDEX"),
    RIGHT_INDEX(20, "RIGHT_INDEX"),
    LEFT_THUMB(21, "LEFT_THUMB"),
    RIGHT_THUMB(22, "RIGHT_THUMB"),
    LEFT_HIP(23, "LEFT_HIP"),
    RIGHT_HIP(24, "RIGHT_HIP"),
    LEFT_KNEE(25, "LEFT_KNEE"),
    RIGHT_KNEE(26, "RIGHT_KNEE"),
    LEFT_ANKLE(27, "LEFT_ANKLE"),
    RIGHT_ANKLE(28, "RIGHT_ANKLE"),
    LEFT_HEEL(29, "LEFT_HEEL"),
    RIGHT_HEEL(30, "RIGHT_HEEL"),
    LEFT_FOOT_INDEX(31, "LEFT_FOOT_INDEX"),
    RIGHT_FOOT_INDEX(32, "RIGHT_FOOT_INDEX")

}
