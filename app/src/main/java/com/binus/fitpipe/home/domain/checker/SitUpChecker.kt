package com.binus.fitpipe.home.domain.checker

import android.util.Log
import com.binus.fitpipe.home.domain.data.LandmarkDataManager
import com.binus.fitpipe.home.domain.state.ExerciseState
import com.binus.fitpipe.home.domain.state.ExerciseStateManager
import com.binus.fitpipe.home.domain.utils.AngleCalculator.getAngleBetweenPoints
import com.binus.fitpipe.home.domain.utils.AngleCalculator.isInTolerance
import com.binus.fitpipe.poselandmarker.ConvertedLandmark
import com.binus.fitpipe.poselandmarker.MediaPipeKeyPointEnum
import kotlin.math.abs

class SitUpChecker(
    private val landmarkDataManager: LandmarkDataManager,
    private val exerciseStateManager: ExerciseStateManager,
    private val onExerciseCompleted: (List<List<ConvertedLandmark>>) -> Unit
) {
    fun checkExercise(convertedLandmarks: List<ConvertedLandmark>): Boolean {
        val requiredPoints = extractRequiredPoints(convertedLandmarks) ?: return false

        if (!isFormCorrect(requiredPoints)) {
            Log.d("SitUpChecker", "Sit Up form is bad")
            return false
        }

        Log.d("SitUpChecker", "Sit Up form is good")
        processExerciseState(convertedLandmarks, requiredPoints)
        return true
    }

    private fun extractRequiredPoints(landmarks: List<ConvertedLandmark>): SitUpPoints? {
        val nose = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.NOSE }
        val leftShoulder = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_SHOULDER }
        val leftHip = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_HIP }
        val leftAnkle = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_ANKLE }
        val leftFoot = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_FOOT_INDEX }
        val leftKnee = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_KNEE }

        return if (nose != null && leftShoulder != null && leftHip != null &&
            leftAnkle != null && leftFoot != null && leftKnee != null) {
            SitUpPoints(nose, leftShoulder, leftHip, leftAnkle, leftFoot, leftKnee)
        } else null
    }

    private fun isFormCorrect(points: SitUpPoints): Boolean {
        val kneeAngle = getAngleBetweenPoints(
            points.leftHip.toFloat2(),
            points.leftKnee.toFloat2(),
            points.leftAnkle.toFloat2()
        )
        val kneeAngleCorrect = kneeAngle.isInTolerance(90f)
        val isFeetOnTheGround = abs(points.leftHip.y - points.leftFoot.y) < 0.1f

        return kneeAngleCorrect && isFeetOnTheGround
    }

    private fun processExerciseState(landmarks: List<ConvertedLandmark>, points: SitUpPoints) {
        val hipAngle = getAngleBetweenPoints(
            points.leftShoulder.toFloat2(),
            points.leftHip.toFloat2(),
            points.leftKnee.toFloat2()
        )

        when (exerciseStateManager.getCurrentState()) {
            ExerciseState.WAITING_TO_START -> checkStartingPosition(landmarks, hipAngle)
            ExerciseState.STARTED -> checkGoingUp(landmarks, hipAngle)
            ExerciseState.GOING_FLEXION -> checkUpMax(landmarks, hipAngle)
            ExerciseState.GOING_EXTENSION -> checkDownMax(landmarks, hipAngle)
            ExerciseState.EXERCISE_COMPLETED -> handleCompleted()
            ExerciseState.EXERCISE_FAILED -> handleFailed()
        }
    }

    private fun checkStartingPosition(landmarks: List<ConvertedLandmark>, hipAngle: Float) {
        if (hipAngle.isInTolerance(130f)) {
            exerciseStateManager.updateState(ExerciseState.STARTED)
            landmarkDataManager.addLandmarks(landmarks)
            Log.d("SitUpChecker", "Sit Up started")
        }
    }

    private fun checkGoingUp(landmarks: List<ConvertedLandmark>, hipAngle: Float) {
        val angleDifference = abs(hipAngle - landmarkDataManager.getLastHipAngle())
        if (hipAngle < landmarkDataManager.getLastHipAngle() && angleDifference > 5f) {
            landmarkDataManager.addLandmarks(landmarks)
            exerciseStateManager.updateState(ExerciseState.GOING_FLEXION)
            Log.d("SitUpChecker", "Sit Up Going Up")
        }
    }

    private fun checkUpMax(landmarks: List<ConvertedLandmark>, hipAngle: Float) {
        if (hipAngle <= landmarkDataManager.getLastHipAngle()) {
            if (hipAngle.isInTolerance(90f)) {
                exerciseStateManager.updateState(ExerciseState.GOING_EXTENSION)
                Log.d("SitUpChecker", "Sit Up going down")
            }
            landmarkDataManager.addLandmarks(landmarks)
        }
    }

    private fun checkDownMax(landmarks: List<ConvertedLandmark>, hipAngle: Float) {
        if (hipAngle >= landmarkDataManager.getLastHipAngle()) {
            landmarkDataManager.addLandmarks(landmarks)
            if (hipAngle.isInTolerance(130f)) {
                exerciseStateManager.updateState(ExerciseState.EXERCISE_COMPLETED)
                Log.d("SitUpChecker", "Sit Up completed")
            }
        }
    }

    private fun handleCompleted() {
        if (landmarkDataManager.getLandmarkCount() < 60) {
            onExerciseCompleted(landmarkDataManager.getAllLandmarks())
        } else {
            Log.d("SitUpChecker", "Too many landmarks: ${landmarkDataManager.getLandmarkCount()}")
            exerciseStateManager.updateState(ExerciseState.EXERCISE_FAILED)
        }
        exerciseStateManager.updateState(ExerciseState.WAITING_TO_START)
        landmarkDataManager.clear()
    }

    private fun handleFailed() {
        landmarkDataManager.clear()
        exerciseStateManager.reset()
    }

    private data class SitUpPoints(
        val nose: ConvertedLandmark,
        val leftShoulder: ConvertedLandmark,
        val leftHip: ConvertedLandmark,
        val leftAnkle: ConvertedLandmark,
        val leftFoot: ConvertedLandmark,
        val leftKnee: ConvertedLandmark
    )
}