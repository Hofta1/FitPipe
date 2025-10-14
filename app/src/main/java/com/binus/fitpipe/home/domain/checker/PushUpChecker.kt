package com.binus.fitpipe.home.domain.checker

import android.util.Log
import com.binus.fitpipe.home.domain.data.LandmarkDataManager
import com.binus.fitpipe.home.domain.state.ExerciseState
import com.binus.fitpipe.home.domain.state.ExerciseStateManager
import com.binus.fitpipe.home.domain.utils.AngleCalculator.getAngleBetweenPoints
import com.binus.fitpipe.home.domain.utils.AngleCalculator.isInTolerance
import com.binus.fitpipe.poselandmarker.ConvertedLandmark
import com.binus.fitpipe.poselandmarker.MediaPipeKeyPointEnum
import dev.romainguy.kotlin.math.Float2
import kotlin.math.abs

class PushUpChecker(
    private val landmarkDataManager: LandmarkDataManager,
    private val exerciseStateManager: ExerciseStateManager,
    private val onExerciseCompleted: (List<List<ConvertedLandmark>>) -> Unit
) {

    fun checkExercise(convertedLandmarks: List<ConvertedLandmark>): Boolean {
        val requiredPoints = extractRequiredPoints(convertedLandmarks) ?: return false

        if (!isFormCorrect(requiredPoints)) {
            Log.d("PushUpChecker", "Push Up form is bad")
            return false
        }

        Log.d("PushUpChecker", "Push Up form is good")
        processExerciseState(convertedLandmarks, requiredPoints)
        return true
    }

    private fun extractRequiredPoints(landmarks: List<ConvertedLandmark>): PushUpPoints? {
        val nose = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.NOSE }
        val leftShoulder = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_SHOULDER }
        val leftHip = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_HIP }
        val leftAnkle = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_ANKLE }
        val leftWrist = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_WRIST }
        val leftElbow = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_ELBOW }

        return if (nose != null && leftShoulder != null && leftHip != null &&
            leftAnkle != null && leftWrist != null && leftElbow != null) {
            PushUpPoints(nose, leftShoulder, leftHip, leftAnkle, leftWrist, leftElbow)
        } else null
    }

    private fun isFormCorrect(points: PushUpPoints): Boolean {
        val neckAngle = getAngleBetweenPoints(
            points.nose.toFloat2(),
            points.leftShoulder.toFloat2(),
            points.leftHip.toFloat2()
        )
        val hipAngle = getAngleBetweenPoints(
            points.leftShoulder.toFloat2(),
            points.leftHip.toFloat2(),
            points.leftAnkle.toFloat2()
        )
        val bodyAngle = getAngleBetweenPoints(
            points.leftShoulder.toFloat2(),
            points.leftAnkle.toFloat2(),
            Float2(points.leftWrist.x, points.leftAnkle.y)
        )

        val bodyNotTooLow = points.nose.y > points.leftWrist.y
        val isBodyStraight = neckAngle.isInTolerance(180f) && hipAngle.isInTolerance(180f)
        val isBodyAngleOkay = bodyAngle < 60f

        return isBodyStraight && isBodyAngleOkay && bodyNotTooLow
    }

    private fun processExerciseState(landmarks: List<ConvertedLandmark>, points: PushUpPoints) {
        val elbowAngle = getAngleBetweenPoints(
            points.leftShoulder.toFloat2(),
            points.leftElbow.toFloat2(),
            points.leftWrist.toFloat2()
        )
        val armAngle = getAngleBetweenPoints(
            points.leftWrist.toFloat2(),
            points.leftShoulder.toFloat2(),
            points.leftHip.toFloat2()
        )

        when (exerciseStateManager.getCurrentState()) {
            ExerciseState.WAITING_TO_START -> checkStartingPosition(landmarks, elbowAngle, armAngle)
            ExerciseState.STARTED -> checkGoingDown(landmarks, elbowAngle)
            ExerciseState.GOING_FLEXION -> checkDownMax(landmarks, elbowAngle)
            ExerciseState.GOING_EXTENSION -> checkUpMax(landmarks, elbowAngle)
            ExerciseState.EXERCISE_COMPLETED -> handleCompleted()
            ExerciseState.EXERCISE_FAILED -> handleFailed()
        }
    }

    private fun checkStartingPosition(landmarks: List<ConvertedLandmark>, elbowAngle: Float, armAngle: Float) {
        if (elbowAngle.isInTolerance(180f) && armAngle.isInTolerance(90f)) {
            exerciseStateManager.updateState(ExerciseState.STARTED)
            landmarkDataManager.addLandmarks(landmarks)
            Log.d("PushUpChecker", "Push Up started")
        }
    }

    private fun checkGoingDown(landmarks: List<ConvertedLandmark>, elbowAngle: Float) {
        val angleDifference = abs(elbowAngle - landmarkDataManager.getLastElbowAngle())
        if (elbowAngle < landmarkDataManager.getLastElbowAngle() && angleDifference > 5f) {
            landmarkDataManager.addLandmarks(landmarks)
            exerciseStateManager.updateState(ExerciseState.GOING_FLEXION)
            Log.d("PushUpChecker", "Push Up going down")
        }
    }

    private fun checkDownMax(landmarks: List<ConvertedLandmark>, elbowAngle: Float) {
        if (elbowAngle <= landmarkDataManager.getLastElbowAngle()) {
            if (elbowAngle.isInTolerance(90f)) {
                exerciseStateManager.updateState(ExerciseState.GOING_EXTENSION)
                Log.d("PushUpChecker", "Push Up going up")
            }
            landmarkDataManager.addLandmarks(landmarks)
        }
    }

    private fun checkUpMax(landmarks: List<ConvertedLandmark>, elbowAngle: Float) {
        if (elbowAngle >= landmarkDataManager.getLastElbowAngle()) {
            if (elbowAngle.isInTolerance(180f)) {
                exerciseStateManager.updateState(ExerciseState.EXERCISE_COMPLETED)
                Log.d("PushUpChecker", "Push Up completed")
            }
            landmarkDataManager.addLandmarks(landmarks)
        } else if (elbowAngle <= 75f) {
            exerciseStateManager.updateState(ExerciseState.EXERCISE_FAILED)
            Log.d("PushUpChecker", "Push Up failed")
        }
    }

    private fun handleCompleted() {
        if (landmarkDataManager.getLandmarkCount() >= 60) {
            Log.d("PushUpChecker", "Too many landmarks: ${landmarkDataManager.getLandmarkCount()}")
            exerciseStateManager.updateState(ExerciseState.EXERCISE_FAILED)
        }else{
            onExerciseCompleted(landmarkDataManager.getAllLandmarks())
        }
    }

    private fun handleFailed() {
        landmarkDataManager.clear()
        exerciseStateManager.reset()
    }

    private data class PushUpPoints(
        val nose: ConvertedLandmark,
        val leftShoulder: ConvertedLandmark,
        val leftHip: ConvertedLandmark,
        val leftAnkle: ConvertedLandmark,
        val leftWrist: ConvertedLandmark,
        val leftElbow: ConvertedLandmark
    )
}