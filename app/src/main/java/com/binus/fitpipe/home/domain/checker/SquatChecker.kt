package com.binus.fitpipe.home.domain.checker

import android.util.Log
import com.binus.fitpipe.home.domain.data.LandmarkDataManager
import com.binus.fitpipe.home.domain.state.ExerciseState
import com.binus.fitpipe.home.domain.state.ExerciseStateManager
import com.binus.fitpipe.home.domain.utils.AngleCalculator.get2dAngleBetweenPoints
import com.binus.fitpipe.home.domain.utils.AngleCalculator.isInTolerance
import com.binus.fitpipe.poselandmarker.ConvertedLandmark
import com.binus.fitpipe.poselandmarker.MediaPipeKeyPointEnum
import kotlin.math.abs
import kotlin.text.get

class SquatChecker (
    private val landmarkDataManager: LandmarkDataManager,
    private val exerciseStateManager: ExerciseStateManager,
    private val onExerciseCompleted: (List<List<ConvertedLandmark>>) -> Unit
): ExerciseChecker() {

    private var isFacingLeft: Boolean = true
    fun checkExercise(convertedLandmarks: List<ConvertedLandmark>): Boolean {
        val requiredPoints = extractRequiredPoints(convertedLandmarks) ?: return false

        if (!isFormCorrect(requiredPoints)) {
            isFormOkay = false
            badFormFrameCount++

            if (badFormFrameCount >= BAD_FORM_THRESHOLD &&
                exerciseStateManager.getCurrentState() != ExerciseState.WAITING_TO_START) {
                exerciseStateManager.updateState(ExerciseState.EXERCISE_FAILED)
                Log.d("SquatChecker", "Exercise failed due to bad form")
            }
            return false
        }

        isFormOkay = true
        processExerciseState(convertedLandmarks, requiredPoints)
        return true
    }

    private fun extractRequiredPoints(landmarks: List<ConvertedLandmark>): SquatPoints? {
        var leftCounter = 0
        var rightCounter = 0
        val nose = landmarks[MediaPipeKeyPointEnum.NOSE.keyId]
        val leftShoulder = landmarks[MediaPipeKeyPointEnum.LEFT_SHOULDER.keyId]
        val rightShoulder = landmarks[MediaPipeKeyPointEnum.RIGHT_SHOULDER.keyId]
        val leftHip = landmarks[MediaPipeKeyPointEnum.LEFT_HIP.keyId]
        val rightHip = landmarks[MediaPipeKeyPointEnum.RIGHT_HIP.keyId]
        val leftKnee = landmarks[MediaPipeKeyPointEnum.LEFT_KNEE.keyId]
        val rightKnee = landmarks[MediaPipeKeyPointEnum.RIGHT_KNEE.keyId]
        val leftAnkle = landmarks[MediaPipeKeyPointEnum.LEFT_ANKLE.keyId]
        val rightAnkle = landmarks[MediaPipeKeyPointEnum.RIGHT_ANKLE.keyId]
        val leftFoot = landmarks[MediaPipeKeyPointEnum.LEFT_FOOT_INDEX.keyId]
        val rightFoot = landmarks[MediaPipeKeyPointEnum.RIGHT_FOOT_INDEX.keyId]

        val bodyPairs = listOf(
            leftShoulder to rightShoulder,
            leftHip to rightHip,
            leftKnee to rightKnee,
            leftAnkle to rightAnkle,
            leftFoot to rightFoot
        )
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
            SquatPoints(nose, leftShoulder, leftHip, leftAnkle, leftFoot, leftKnee)
        }else{
            isFacingLeft = false
            SquatPoints(nose, rightShoulder, rightHip, rightAnkle, rightFoot, rightKnee)
        }
    }

    private fun isFormCorrect(points: SquatPoints): Boolean {
        var isShoulderDifferenceBig = false
        if(landmarkDataManager.getLandmarkCount() > 1){
            val firstShoulderXFloat = landmarkDataManager.getFirstShoulderX()
            val currentShoulderXFloat = points.shoulder.x
            val shoulderXDifference = abs(currentShoulderXFloat - firstShoulderXFloat)
            isShoulderDifferenceBig = shoulderXDifference > 0.05f
            Log.d("SquatChecker", "shoulder difference: $shoulderXDifference")
        }

        val kneeOverFoot = if(isFacingLeft) points.knee.x > points.foot.x else points.knee.x < points.foot.x
        if (kneeOverFoot) {
            statusString = "Knee can't be over foot"
        }

        return !isShoulderDifferenceBig && !kneeOverFoot
    }

    private fun processExerciseState(landmarks: List<ConvertedLandmark>, points: SquatPoints) {
        val hipAngle = get2dAngleBetweenPoints(
            points.shoulder.toFloat2(),
            points.hip.toFloat2(),
            points.knee.toFloat2()
        )

        val kneeAngle = get2dAngleBetweenPoints(
            points.hip.toFloat2(),
            points.knee.toFloat2(),
            points.ankle.toFloat2()
        )

        when (exerciseStateManager.getCurrentState()) {
            ExerciseState.WAITING_TO_START -> checkStartingPosition(landmarks, hipAngle, kneeAngle)
            ExerciseState.STARTED -> checkGoingDown(landmarks, hipAngle, kneeAngle)
            ExerciseState.GOING_FLEXION -> checkDownMax(landmarks, hipAngle, kneeAngle)
            ExerciseState.GOING_EXTENSION -> checkUpMax(landmarks, hipAngle, kneeAngle)
            ExerciseState.EXERCISE_COMPLETED -> handleCompleted()
            ExerciseState.EXERCISE_FAILED -> handleFailed()
        }
    }

    private fun checkStartingPosition(landmarks: List<ConvertedLandmark>, hipAngle: Float, kneeAngle: Float) {
        val isBodyStraight = kneeAngle.isInTolerance(180f) && hipAngle.isInTolerance(180f)
        if (isBodyStraight) {
            exerciseStateManager.updateState(ExerciseState.STARTED)
            landmarkDataManager.addLandmarks(landmarks)
            Log.d("SquatChecker", "Squat started")
        }
    }

    private fun checkGoingDown(landmarks: List<ConvertedLandmark>, hipAngle: Float, kneeAngle: Float) {
        val hipAngleDifference = abs(hipAngle - landmarkDataManager.getLastHipAngle())
        val kneeAngleDifference = abs(kneeAngle - landmarkDataManager.getLastKneeAngle())
        val hipAngleOkay = hipAngle < landmarkDataManager.getLastHipAngle() && hipAngleDifference > 5f
        val kneeAngleOkay = kneeAngle < landmarkDataManager.getLastKneeAngle() && kneeAngleDifference > 5f
        if (hipAngleOkay && kneeAngleOkay) {
            landmarkDataManager.addLandmarks(landmarks)
            exerciseStateManager.updateState(ExerciseState.GOING_FLEXION)
            Log.d("SquatChecker", "Squat Going down")
        }
    }

    private fun checkDownMax(landmarks: List<ConvertedLandmark>, hipAngle: Float, kneeAngle: Float) {
        if (hipAngle <= landmarkDataManager.getLastHipAngle() || kneeAngle <= landmarkDataManager.getLastKneeAngle()) {
            if (hipAngle < 70f && kneeAngle < 85f) {
                exerciseStateManager.updateState(ExerciseState.GOING_EXTENSION)
                Log.d("SquatChecker", "Squat going up")
            }
            landmarkDataManager.addLandmarks(landmarks)
        }else if (kneeAngle > 150f) { //when the user goes up again without reaching the down max
            Log.d("SquatChecker", "Squat failed, not deep enough")
            exerciseStateManager.updateState(ExerciseState.EXERCISE_FAILED)
        }
    }

    private fun checkUpMax(landmarks: List<ConvertedLandmark>, hipAngle: Float, kneeAngle: Float) {
        if (hipAngle >= landmarkDataManager.getLastHipAngle() || kneeAngle >= landmarkDataManager.getLastKneeAngle()) {
            if (hipAngle.isInTolerance(180f) && kneeAngle.isInTolerance(180f)) {
                exerciseStateManager.updateState(ExerciseState.EXERCISE_COMPLETED)
                Log.d("SquatChecker", "Squat completed")
            }
            landmarkDataManager.addLandmarks(landmarks)
        }else if (kneeAngle < 50f) { //when user goes too deep
            Log.d("SquatChecker", "Squat failed, too deep")
            exerciseStateManager.updateState(ExerciseState.EXERCISE_FAILED)
        }
    }

    private fun handleCompleted() {
        if (landmarkDataManager.getLandmarkCount() < 30) {
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
        badFormFrameCount = 0
        exerciseStateManager.reset()
    }

    private data class SquatPoints(
        val nose: ConvertedLandmark,
        val shoulder: ConvertedLandmark,
        val hip: ConvertedLandmark,
        val ankle: ConvertedLandmark,
        val foot: ConvertedLandmark,
        val knee: ConvertedLandmark
    )
}