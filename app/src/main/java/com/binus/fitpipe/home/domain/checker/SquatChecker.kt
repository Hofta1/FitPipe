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

class SquatChecker (
    private val landmarkDataManager: LandmarkDataManager,
    private val exerciseStateManager: ExerciseStateManager,
    private val onExerciseCompleted: (List<List<ConvertedLandmark>>) -> Unit,
    private val onUpdateStatusString: (String) -> Unit,
    private val onUpdateState: (ExerciseState) -> Unit
): ExerciseChecker() {

    private var isUsingLeft: Boolean = true
    fun checkExercise(convertedLandmarks: List<ConvertedLandmark>): Boolean {
        val requiredPoints = extractRequiredPoints(convertedLandmarks) ?: return false
        if(exerciseStateManager.getCurrentState() == ExerciseState.EXERCISE_FAILED){
            handleFailed()
        }

        if (!isFormCorrect(requiredPoints)) {
            isFormOkay = false
            badFormFrameCount++
            return false
        }

        if(exerciseStateManager.getCurrentState() != ExerciseState.WAITING_TO_START && badFormFrameCount >= BAD_FORM_THRESHOLD){
            exerciseStateManager.updateState(ExerciseState.EXERCISE_FAILED)
            Log.d("SquatChecker", "Exercise failed due to bad form")
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
                statusString = "Bad visibility"
                badFormFrameCount++
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
            isUsingLeft = true
            SquatPoints(nose, leftShoulder, leftHip, leftAnkle, leftFoot, leftKnee)
        }else{
            isUsingLeft = false
            SquatPoints(nose, rightShoulder, rightHip, rightAnkle, rightFoot, rightKnee)
        }
    }

    private fun isFormCorrect(points: SquatPoints): Boolean {

        val kneeOverFoot = if(isUsingLeft) {
            points.foot.x - points.knee.x < -0.07f
        } else {
            points.knee.x - points.foot.x < -0.07f
        }
        if (kneeOverFoot) {
            statusString = "Knee can't be over foot"
        }

        return  !kneeOverFoot
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
        onUpdateState(exerciseStateManager.getCurrentState())
        val isBodyStraight = kneeAngle.isInTolerance(180f, tolerance = 40f) && hipAngle.isInTolerance(180f, tolerance = 40f)
        if (isBodyStraight) {
            exerciseStateManager.updateState(ExerciseState.STARTED)
            landmarkDataManager.addLandmarks(landmarks)
            Log.d("SquatChecker", "Squat started")
        }
    }

    private fun checkGoingDown(landmarks: List<ConvertedLandmark>, hipAngle: Float, kneeAngle: Float) {
        val hipAngleDifference = abs(hipAngle - landmarkDataManager.getLastHipAngle(isUsingLeft))
        val kneeAngleDifference = abs(kneeAngle - landmarkDataManager.getLastKneeAngle())
        val hipAngleOkay = hipAngle < landmarkDataManager.getLastHipAngle(isUsingLeft) && hipAngleDifference > 10f
        val kneeAngleOkay = kneeAngle < landmarkDataManager.getLastKneeAngle() && kneeAngleDifference > 10f
        if (hipAngleOkay && kneeAngleOkay) {
            landmarkDataManager.addLandmarks(landmarks)
            exerciseStateManager.updateState(ExerciseState.GOING_FLEXION)
            onUpdateState(exerciseStateManager.getCurrentState())
            Log.d("SquatChecker", "Squat Going down")
        }else{
            badFormFrameCount++
        }
    }

    private fun checkDownMax(landmarks: List<ConvertedLandmark>, hipAngle: Float, kneeAngle: Float) {
        if (hipAngle <= landmarkDataManager.getLastHipAngle(isUsingLeft) || kneeAngle <= landmarkDataManager.getLastKneeAngle()) {
            Log.d("SquatChecker", "Current Hip Angle: $hipAngle, Current Knee Angle: $kneeAngle")
            landmarkDataManager.addLandmarks(landmarks)
            if (hipAngle < 85f && kneeAngle < 90f) {
                exerciseStateManager.updateState(ExerciseState.GOING_EXTENSION)
                onUpdateState(exerciseStateManager.getCurrentState())
                Log.d("SquatChecker", "Squat going up")
            }
        }else if (kneeAngle > 150f) { //when the user goes up again without reaching the down max
            Log.d("SquatChecker", "Knee Angle too high: $kneeAngle")
            statusString = "Did not go down enough"
            badFormFrameCount++
        }
    }

    private fun checkUpMax(landmarks: List<ConvertedLandmark>, hipAngle: Float, kneeAngle: Float) {
        Log.d("SquatChecker", "Current Hip Angle: $hipAngle, Current Knee Angle: $kneeAngle")

        landmarkDataManager.addLandmarks(landmarks)
        if (hipAngle >= landmarkDataManager.getLastHipAngle(isUsingLeft) || kneeAngle >= landmarkDataManager.getLastKneeAngle()) {
            if (hipAngle.isInTolerance(180f, tolerance = 40f) && kneeAngle.isInTolerance(180f, tolerance = 40f)) {
                exerciseStateManager.updateState(ExerciseState.EXERCISE_COMPLETED)
                Log.d("SquatChecker", "Squat completed")
            }
        }
    }

    private fun handleCompleted() {
        if (landmarkDataManager.getLandmarkCount() < 60) {
            onExerciseCompleted(landmarkDataManager.getAllLandmarks())
        } else {
            exerciseStateManager.updateState(ExerciseState.EXERCISE_FAILED)
        }
        exerciseStateManager.updateState(ExerciseState.WAITING_TO_START)
        badFormFrameCount = 0
        landmarkDataManager.clear()
    }

    private fun handleFailed() {
        landmarkDataManager.clear()
        onUpdateStatusString(statusString)
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