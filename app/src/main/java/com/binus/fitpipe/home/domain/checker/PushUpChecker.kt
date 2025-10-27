package com.binus.fitpipe.home.domain.checker

import android.util.Log
import com.binus.fitpipe.home.domain.data.LandmarkDataManager
import com.binus.fitpipe.home.domain.state.ExerciseState
import com.binus.fitpipe.home.domain.state.ExerciseStateManager
import com.binus.fitpipe.home.domain.utils.AngleCalculator.get2dAngleBetweenPoints
import com.binus.fitpipe.home.domain.utils.AngleCalculator.isInTolerance
import com.binus.fitpipe.poselandmarker.ConvertedLandmark
import com.binus.fitpipe.poselandmarker.MediaPipeKeyPointEnum
import dev.romainguy.kotlin.math.Float2
import kotlin.math.abs
import kotlin.text.get

class PushUpChecker(
    private val landmarkDataManager: LandmarkDataManager,
    private val exerciseStateManager: ExerciseStateManager,
    private val onExerciseCompleted: (List<List<ConvertedLandmark>>) -> Unit
): ExerciseChecker() {
    fun checkExercise(convertedLandmarks: List<ConvertedLandmark>): Boolean {
        val requiredPoints = extractRequiredPoints(convertedLandmarks) ?: return false

        if (!isFormCorrect(requiredPoints)) {
            isFormOkay = false
            return false
        }

        isFormOkay = true
        processExerciseState(convertedLandmarks, requiredPoints)
        return true
    }

    private fun extractRequiredPoints(landmarks: List<ConvertedLandmark>): PushUpPoints? {
        val nose = landmarks[MediaPipeKeyPointEnum.NOSE.keyId]
        val leftEye = landmarks[MediaPipeKeyPointEnum.LEFT_EYE.keyId]
        val rightEye = landmarks[MediaPipeKeyPointEnum.RIGHT_EYE.keyId]
        val leftShoulder = landmarks[MediaPipeKeyPointEnum.LEFT_SHOULDER.keyId]
        val rightShoulder = landmarks[MediaPipeKeyPointEnum.RIGHT_SHOULDER.keyId]
        val leftHip = landmarks[MediaPipeKeyPointEnum.LEFT_HIP.keyId]
        val rightHip = landmarks[MediaPipeKeyPointEnum.RIGHT_HIP.keyId]
        val leftAnkle = landmarks[MediaPipeKeyPointEnum.LEFT_ANKLE.keyId]
        val rightAnkle = landmarks[MediaPipeKeyPointEnum.RIGHT_ANKLE.keyId]
        val leftWrist = landmarks[MediaPipeKeyPointEnum.LEFT_WRIST.keyId]
        val rightWrist = landmarks[MediaPipeKeyPointEnum.RIGHT_WRIST.keyId]
        val leftElbow = landmarks[MediaPipeKeyPointEnum.LEFT_ELBOW.keyId]
        val rightElbow = landmarks[MediaPipeKeyPointEnum.RIGHT_ELBOW.keyId]

        val bodyPairs = listOf(
            leftShoulder to rightShoulder,
            leftHip to rightHip,
            leftAnkle to rightAnkle,
            leftWrist to rightWrist,
            leftElbow to rightElbow
        )
        var leftCounter = 0
        var rightCounter = 0

        for (pair in bodyPairs) {
            if(pair.first.visibility.get() < 0.75f && pair.second.visibility.get() < 0.75f){
                exerciseStateManager.updateState(ExerciseState.EXERCISE_FAILED)
                return null
            }
            if (pair.first.visibility.get() >= pair.second.visibility.get()) {
                leftCounter++
            } else {
                rightCounter++
            }
            if (pair.first.presence.get() >= pair.second.presence.get()) {
                leftCounter++
            } else {
                rightCounter++
            }
        }

        return if (leftCounter > rightCounter){
            PushUpPoints(
                nose = nose,
                eye = leftEye,
                shoulder = leftShoulder,
                hip = leftHip,
                ankle = leftAnkle,
                wrist = leftWrist,
                elbow = leftElbow
            )
        } else{
            PushUpPoints(
                nose = nose,
                eye = landmarks[MediaPipeKeyPointEnum.RIGHT_EYE.keyId],
                shoulder = rightShoulder,
                hip = rightHip,
                ankle = rightAnkle,
                wrist = rightWrist,
                elbow = rightElbow
            )
        }
    }

    private fun isFormCorrect(points: PushUpPoints): Boolean {
        val neckAngle = get2dAngleBetweenPoints(
            points.eye.toFloat2(),
            points.shoulder.toFloat2(),
            points.hip.toFloat2()
        )
        val hipAngle = get2dAngleBetweenPoints(
            points.shoulder.toFloat2(),
            points.hip.toFloat2(),
            points.ankle.toFloat2()
        )
        val bodyAngle = get2dAngleBetweenPoints(
            points.shoulder.toFloat2(),
            points.ankle.toFloat2(),
            Float2(points.wrist.x, points.ankle.y)
        )

        val bodyNotTooLow = points.eye.y > points.wrist.y - 0.1f
        val isBodyStraight = neckAngle.isInTolerance(150f, tolerance = 40f) && hipAngle.isInTolerance(170f)
        val isBodyAngleOkay = bodyAngle < 60f

        return isBodyStraight && isBodyAngleOkay && bodyNotTooLow
    }

    private fun processExerciseState(landmarks: List<ConvertedLandmark>, points: PushUpPoints) {
        val elbowAngle = get2dAngleBetweenPoints(
            points.shoulder.toFloat2(),
            points.elbow.toFloat2(),
            points.wrist.toFloat2()
        )
        val armAngle = get2dAngleBetweenPoints(
            points.wrist.toFloat2(),
            points.shoulder.toFloat2(),
            points.hip.toFloat2()
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
        if (elbowAngle.isInTolerance(180f) && armAngle.isInTolerance(75f, tolerance = 40f)) {
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
        } else if (elbowAngle <= 45f) {
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
        landmarkDataManager.clear()
        exerciseStateManager.reset()
    }

    private fun handleFailed() {
        landmarkDataManager.clear()
        exerciseStateManager.reset()
        badFormFrameCount = 0
    }

    private data class PushUpPoints(
        val nose: ConvertedLandmark,
        val eye: ConvertedLandmark,
        val shoulder: ConvertedLandmark,
        val hip: ConvertedLandmark,
        val ankle: ConvertedLandmark,
        val wrist: ConvertedLandmark,
        val elbow: ConvertedLandmark
    )
}