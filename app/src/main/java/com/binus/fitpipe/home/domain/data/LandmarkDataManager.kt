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
        return lastLandmark?.let { landmark ->
            val lastShoulder = landmark[MediaPipeKeyPointEnum.LEFT_SHOULDER.keyId]
            val lastElbow = landmark[MediaPipeKeyPointEnum.LEFT_ELBOW.keyId]
            val lastWrist = landmark[MediaPipeKeyPointEnum.LEFT_WRIST.keyId]
            get2dAngleBetweenPoints(
                lastShoulder.toFloat2(),
                lastElbow.toFloat2(),
                lastWrist.toFloat2(),
            )
        } ?: 0f
    }

    fun getLastHipAngle(): Float {
        val lastLandmark = landmarkList.lastOrNull()
        return lastLandmark?.let { landmark ->
            val lastShoulder = landmark[MediaPipeKeyPointEnum.LEFT_SHOULDER.keyId]
            val lastHip = landmark[MediaPipeKeyPointEnum.LEFT_HIP.keyId]
            val leftKnee = landmark[MediaPipeKeyPointEnum.LEFT_KNEE.keyId]
            get2dAngleBetweenPoints(
                lastShoulder.toFloat2(),
                lastHip.toFloat2(),
                leftKnee.toFloat2(),
            )
        }?: 0f
    }

    fun getLastKneeAngle(): Float {
        val lastLandmark = landmarkList.lastOrNull()
        return lastLandmark?.let { landmark ->
            val lastHip = landmark[MediaPipeKeyPointEnum.LEFT_HIP.keyId]
            val leftKnee = landmark[MediaPipeKeyPointEnum.LEFT_KNEE.keyId]
            val leftAnkle = landmark[MediaPipeKeyPointEnum.LEFT_ANKLE.keyId]
            get2dAngleBetweenPoints(
                lastHip.toFloat2(),
                leftKnee.toFloat2(),
                leftAnkle.toFloat2(),
            )
        } ?: 0f
    }

    fun getAllLandmarks(): List<List<ConvertedLandmark>> = landmarkList.toList()

    fun getLandmarkCount(): Int = landmarkList.size

    fun clear() {
        landmarkList.clear()
    }

    fun getFirstShoulderX(): Float{
        return landmarkList.firstOrNull()?.let { landmark ->
            landmark[MediaPipeKeyPointEnum.LEFT_SHOULDER.keyId].toFloat2().x
        } ?: 0f
    }

    fun getFirstHipAngle(): Float {
        val firstLandmark = landmarkList.firstOrNull()
        return firstLandmark?.let{ landmark ->
            val firstShoulder = landmark[MediaPipeKeyPointEnum.LEFT_SHOULDER.keyId]
            val firstHip = landmark[MediaPipeKeyPointEnum.LEFT_HIP.keyId]
            val firstKnee = landmark[MediaPipeKeyPointEnum.LEFT_KNEE.keyId]
            get2dAngleBetweenPoints(
                firstShoulder.toFloat2(),
                firstHip.toFloat2(),
                firstKnee.toFloat2(),
            )
        } ?: 0f
    }

    fun getFirstElbowAngle(): Float {
        val firstLandmark = landmarkList.firstOrNull()
        return firstLandmark?.let{ landmark ->
            val firstShoulder = landmark[MediaPipeKeyPointEnum.LEFT_SHOULDER.keyId]
            val firstElbow = landmark[MediaPipeKeyPointEnum.LEFT_ELBOW.keyId]
            val firstWrist = landmark[MediaPipeKeyPointEnum.LEFT_WRIST.keyId]
            get2dAngleBetweenPoints(
                firstShoulder.toFloat2(),
                firstElbow.toFloat2(),
                firstWrist.toFloat2(),
            )
        } ?: 0f
    }
}