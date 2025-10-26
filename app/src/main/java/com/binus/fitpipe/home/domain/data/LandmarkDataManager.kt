package com.binus.fitpipe.home.domain.data

import com.binus.fitpipe.home.domain.utils.AngleCalculator.get2dAngleBetweenPoints
import com.binus.fitpipe.poselandmarker.ConvertedLandmark
import com.binus.fitpipe.poselandmarker.MediaPipeKeyPointEnum

class LandmarkDataManager {
    private val landmarkList = mutableListOf<List<ConvertedLandmark>>()

    fun addLandmarks(landmarks: List<ConvertedLandmark>) {
        landmarkList.add(landmarks)
    }

    fun getLastElbowAngle(): Float {
        val lastLandmark = landmarkList.lastOrNull()
        val lastShoulder = lastLandmark?.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_SHOULDER }
        val lastElbow = lastLandmark?.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_ELBOW }
        val lastWrist = lastLandmark?.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_WRIST }

        return if (lastShoulder != null && lastElbow != null && lastWrist != null) {
            get2dAngleBetweenPoints(
                lastShoulder.toFloat2(),
                lastElbow.toFloat2(),
                lastWrist.toFloat2(),
            )
        } else {
            0f
        }
    }

    fun getLastHipAngle(): Float {
        val lastLandmark = landmarkList.lastOrNull()
        val lastShoulder = lastLandmark?.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_SHOULDER }
        val lastHip = lastLandmark?.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_HIP }
        val leftKnee = lastLandmark?.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_KNEE }

        return if (lastShoulder != null && lastHip != null && leftKnee != null) {
            get2dAngleBetweenPoints(
                lastShoulder.toFloat2(),
                lastHip.toFloat2(),
                leftKnee.toFloat2(),
            )
        } else {
            0f
        }
    }

    fun getLastKneeAngle(): Float {
        val lastLandmark = landmarkList.lastOrNull()
        val lastHip = lastLandmark?.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_HIP }
        val leftKnee = lastLandmark?.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_KNEE }
        val leftAnkle = lastLandmark?.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_ANKLE }

        return if (lastHip != null && leftKnee != null && leftAnkle != null) {
            get2dAngleBetweenPoints(
                lastHip.toFloat2(),
                leftKnee.toFloat2(),
                leftAnkle.toFloat2(),
            )
        } else {
            0f
        }
    }

    fun getAllLandmarks(): List<List<ConvertedLandmark>> = landmarkList.toList()

    fun getLandmarkCount(): Int = landmarkList.size

    fun clear() {
        landmarkList.clear()
    }

    fun getFirstShoulderX(): Float{
        return landmarkList.firstOrNull()?.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_SHOULDER }?.toFloat2()?.x ?: 0f
    }
}