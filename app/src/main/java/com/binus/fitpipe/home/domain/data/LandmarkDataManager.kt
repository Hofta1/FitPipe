package com.binus.fitpipe.home.domain.data

import com.binus.fitpipe.home.domain.utils.AngleCalculator.get2dAngleBetweenPoints
import com.binus.fitpipe.poselandmarker.ConvertedLandmark
import com.binus.fitpipe.poselandmarker.MediaPipeKeyPointEnum
import dev.romainguy.kotlin.math.Float2
import java.util.Optional

class LandmarkDataManager {
    private val landmarkList = mutableListOf<List<ConvertedLandmark>>()

    fun addLandmarks(landmarks: List<ConvertedLandmark>) {
        landmarkList.add(landmarks)
    }

    fun getLastY(enum: MediaPipeKeyPointEnum): Float {
        val lastLandmark = landmarkList.lastOrNull()
        return lastLandmark?.let { landmark ->
            val lastY = landmark[enum.keyId].y
            lastY
        } ?: 0f
    }

    fun getFirstKneeAngle(isLeft: Boolean): Float {
        val firstLandmark = landmarkList.firstOrNull()
        return firstLandmark?.let { landmark ->
            if (isLeft) {
                val leftHip = landmark[MediaPipeKeyPointEnum.LEFT_HIP.keyId]
                val leftKnee = landmark[MediaPipeKeyPointEnum.LEFT_KNEE.keyId]
                val leftAnkle = landmark[MediaPipeKeyPointEnum.LEFT_ANKLE.keyId]
                get2dAngleBetweenPoints(
                    leftHip.toFloat2(),
                    leftKnee.toFloat2(),
                    leftAnkle.toFloat2(),
                )
            } else {
                val rightHip = landmark[MediaPipeKeyPointEnum.RIGHT_HIP.keyId]
                val rightKnee = landmark[MediaPipeKeyPointEnum.RIGHT_KNEE.keyId]
                val rightAnkle = landmark[MediaPipeKeyPointEnum.RIGHT_ANKLE.keyId]
                get2dAngleBetweenPoints(
                    rightHip.toFloat2(),
                    rightKnee.toFloat2(),
                    rightAnkle.toFloat2(),
                )
            }
        } ?: 0f
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

    fun getLastHipAngle(isLeft: Boolean): Float {
        val lastLandmark = landmarkList.lastOrNull()
        return lastLandmark?.let { landmark ->
            val lastShoulder = if (isLeft) landmark[MediaPipeKeyPointEnum.LEFT_SHOULDER.keyId] else landmark[MediaPipeKeyPointEnum.RIGHT_SHOULDER.keyId]
            val lastHip = if (isLeft) landmark[MediaPipeKeyPointEnum.LEFT_HIP.keyId] else landmark[MediaPipeKeyPointEnum.RIGHT_HIP.keyId]
            val leftKnee = if (isLeft) landmark[MediaPipeKeyPointEnum.LEFT_KNEE.keyId] else landmark[MediaPipeKeyPointEnum.RIGHT_KNEE.keyId]
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

    fun getLastLeftArmAngle(): Float {
        val lastLandmark = landmarkList.lastOrNull()
        return lastLandmark?.let { landmark ->
            val leftHip = landmark[MediaPipeKeyPointEnum.LEFT_HIP.keyId]
            val leftShoulder = landmark[MediaPipeKeyPointEnum.LEFT_SHOULDER.keyId]
            val leftWrist = landmark[MediaPipeKeyPointEnum.LEFT_WRIST.keyId]
            get2dAngleBetweenPoints(
                leftWrist.toFloat2(),
                leftShoulder.toFloat2(),
                leftHip.toFloat2(),
            )
        } ?: 0f
    }

    fun getLastRightArmAngle(): Float {
        val lastLandmark = landmarkList.lastOrNull()
        return lastLandmark?.let { landmark ->
            val rightHip = landmark[MediaPipeKeyPointEnum.RIGHT_HIP.keyId]
            val rightShoulder = landmark[MediaPipeKeyPointEnum.RIGHT_SHOULDER.keyId]
            val rightWrist = landmark[MediaPipeKeyPointEnum.RIGHT_WRIST.keyId]
            get2dAngleBetweenPoints(
                rightWrist.toFloat2(),
                rightShoulder.toFloat2(),
                rightHip.toFloat2(),
            )
        } ?: 0f
    }
    fun getLastLeftHipAngle(): Float {
        val lastLandmark = landmarkList.lastOrNull()
        return lastLandmark?.let { landmark ->
            val leftAnkle = landmark[MediaPipeKeyPointEnum.LEFT_ANKLE.keyId]
            val leftHip = landmark[MediaPipeKeyPointEnum.LEFT_HIP.keyId]
            val rightHip = landmark[MediaPipeKeyPointEnum.RIGHT_HIP.keyId]
            val middleHip = getMiddlePointOfLandmark(leftHip, rightHip)
            get2dAngleBetweenPoints(
                leftAnkle.toFloat2(),
                middleHip.toFloat2(),
                Float2(middleHip.x, leftAnkle.y),
            )
        } ?: 0f
    }
    fun getLastRightHipAngle(): Float {
        val lastLandmark = landmarkList.lastOrNull()
        return lastLandmark?.let { landmark ->
            val rightAnkle = landmark[MediaPipeKeyPointEnum.RIGHT_ANKLE.keyId]
            val rightHip = landmark[MediaPipeKeyPointEnum.RIGHT_HIP.keyId]
            val leftHip = landmark[MediaPipeKeyPointEnum.LEFT_HIP.keyId]
            val middleHip = getMiddlePointOfLandmark(leftHip, rightHip)
            get2dAngleBetweenPoints(
                rightAnkle.toFloat2(),
                middleHip.toFloat2(),
                Float2(middleHip.x, rightAnkle.y),
            )
        } ?: 0f
    }

    fun getAllLandmarks(): List<List<ConvertedLandmark>> = landmarkList.toList()

    fun getLandmarkCount(): Int = landmarkList.size

    fun clear() {
        landmarkList.clear()
    }

    fun getStartingX(enumId: Int): Float{
        return landmarkList.firstOrNull()?.let { landmark ->
            landmark[enumId].x
        } ?: 0f
    }

    fun getMiddlePointOfLandmark(left: ConvertedLandmark, right: ConvertedLandmark): ConvertedLandmark {
        val midX = (left.x + right.x) / 2
        val midY = (left.y + right.y) / 2
        val midZ = (left.z + right.z) / 2
        val midVisibility = Optional.of((left.visibility.get() + right.visibility.get()) / 2)
        val midPresence = Optional.of((left.presence.get() + right.presence.get()) / 2)
        return ConvertedLandmark(
            x = midX,
            y = midY,
            z = midZ,
            visibility = midVisibility,
            presence = midPresence,
        )
    }

    fun getStartingY(enumId: Int): Float {
        val firstLandmark = landmarkList.firstOrNull()
        return firstLandmark?.let { landmark ->
            val point = landmark[enumId]
            point.y
        } ?: 0f
    }
}