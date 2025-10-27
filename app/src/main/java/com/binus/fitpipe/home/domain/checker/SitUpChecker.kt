package com.binus.fitpipe.home.domain.checker

import android.util.Log
import com.binus.fitpipe.home.domain.data.LandmarkDataManager
import com.binus.fitpipe.home.domain.state.ExerciseState
import com.binus.fitpipe.home.domain.state.ExerciseStateManager
import com.binus.fitpipe.home.domain.utils.AngleCalculator.get3dAngleBetweenPoints
import com.binus.fitpipe.home.domain.utils.AngleCalculator.isInTolerance
import com.binus.fitpipe.poselandmarker.ConvertedLandmark
import com.binus.fitpipe.poselandmarker.MediaPipeKeyPointEnum
import kotlin.math.abs
import kotlin.text.get

class SitUpChecker(
    private val landmarkDataManager: LandmarkDataManager,
    private val exerciseStateManager: ExerciseStateManager,
    private val onExerciseCompleted: (List<List<ConvertedLandmark>>) -> Unit
): ExerciseChecker() {
    fun checkExercise(convertedLandmarks: List<ConvertedLandmark>): Boolean {
        val requiredPoints = extractRequiredPoints(convertedLandmarks) ?: return false

        if (!isFormCorrect(requiredPoints)) {
            isFormOkay = false
            Log.d("SitUpChecker", "Sit Up form is bad")
            badFormFrameCount++

            if (badFormFrameCount >= BAD_FORM_THRESHOLD &&
                exerciseStateManager.getCurrentState() != ExerciseState.WAITING_TO_START) {
                exerciseStateManager.updateState(ExerciseState.EXERCISE_FAILED)
                Log.d("SitUpChecker", "Exercise failed due to bad form")
            }
            return false
        }
        isFormOkay = true
        badFormFrameCount = 0
        processExerciseState(convertedLandmarks, requiredPoints)
        return true
    }

    private fun extractRequiredPoints(landmarks: List<ConvertedLandmark>): SitUpPoints? {
        val nose = landmarks[MediaPipeKeyPointEnum.NOSE.keyId]
        val leftShoulder = landmarks[MediaPipeKeyPointEnum.LEFT_SHOULDER.keyId]
        val rightShoulder = landmarks[MediaPipeKeyPointEnum.RIGHT_SHOULDER.keyId]
        val leftHip = landmarks[MediaPipeKeyPointEnum.LEFT_HIP.keyId]
        val rightHip = landmarks[MediaPipeKeyPointEnum.RIGHT_HIP.keyId]
        val leftAnkle = landmarks[MediaPipeKeyPointEnum.LEFT_ANKLE.keyId]
        val rightAnkle = landmarks[MediaPipeKeyPointEnum.RIGHT_ANKLE.keyId]
        val leftFoot = landmarks[MediaPipeKeyPointEnum.LEFT_FOOT_INDEX.keyId]
        val rightFoot = landmarks[MediaPipeKeyPointEnum.RIGHT_FOOT_INDEX.keyId]
        val leftKnee = landmarks[MediaPipeKeyPointEnum.LEFT_KNEE.keyId]
        val rightKnee = landmarks[MediaPipeKeyPointEnum.RIGHT_KNEE.keyId]

        val bodyPairs = listOf(
            leftShoulder to rightShoulder,
            leftHip to rightHip,
            leftAnkle to rightAnkle,
            leftFoot to rightFoot,
            leftKnee to rightKnee
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

        return if(leftCounter > rightCounter){
            SitUpPoints(
                nose = nose,
                leftShoulder = leftShoulder,
                leftHip = leftHip,
                leftAnkle = leftAnkle,
                leftFoot = leftFoot,
                leftKnee = leftKnee
            )
        } else{
            SitUpPoints(
                nose = nose,
                leftShoulder = rightShoulder,
                leftHip = rightHip,
                leftAnkle = rightAnkle,
                leftFoot = rightFoot,
                leftKnee = rightKnee
            )
        }
    }

    private fun isFormCorrect(points: SitUpPoints): Boolean {
        val idealKneeAngle = 80f
        val kneeAngle = get3dAngleBetweenPoints(
            points.leftHip.toFloat3(),
            points.leftKnee.toFloat3(),
            points.leftAnkle.toFloat3()
        )
        val kneeAngleCorrect = kneeAngle.isInTolerance(idealKneeAngle, tolerance = 60f)
        val isFeetOnTheGround = abs(points.leftHip.y - points.leftFoot.y) < 0.1f

        if(!kneeAngleCorrect) {
            statusString = if(kneeAngle > idealKneeAngle) {
                "Knees Not Bent Enough"
            }else{
                "Knees Bent Too Much"
            }
        } else if(!isFeetOnTheGround) {
            statusString = "Feet Not On Ground"
        }

        return kneeAngleCorrect && isFeetOnTheGround
    }

    private fun processExerciseState(landmarks: List<ConvertedLandmark>, points: SitUpPoints) {
        val hipAngle = get3dAngleBetweenPoints(
            points.leftShoulder.toFloat3(),
            points.leftHip.toFloat3(),
            points.leftKnee.toFloat3()
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
        if (hipAngle.isInTolerance(130f, tolerance = 40f)) {
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
        }else if(hipAngle > 130f){
            // User going back to starting position without completing the sit up
            exerciseStateManager.updateState(ExerciseState.EXERCISE_FAILED)
            statusString = "Did not complete the sit up"
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
            if (hipAngle.isInTolerance(landmarkDataManager.getFirstHipAngle())) {
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
        exerciseStateManager.reset()
        landmarkDataManager.clear()
    }

    private fun handleFailed() {
        landmarkDataManager.clear()
        exerciseStateManager.reset()
        badFormFrameCount = 0
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