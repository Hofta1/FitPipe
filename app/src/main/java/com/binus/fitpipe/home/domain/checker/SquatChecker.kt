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
    private val onExerciseCompleted: (List<List<ConvertedLandmark>>) -> Unit
) {
    var isFormOkay = false
    var statusString = ""
    private var badFormFrameCount = 0
    private val BAD_FORM_THRESHOLD = 6

    fun getFormattedStatus(): String {
        return statusString
    }

    fun getFormStatus(): Boolean {
        return isFormOkay
    }
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
        val nose = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.NOSE }
        val leftShoulder = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_SHOULDER }
        val leftHip = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_HIP }
        val leftKnee = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_KNEE }
        val leftAnkle = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_ANKLE }
        val leftFoot = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_FOOT_INDEX }

        return if (nose != null && leftShoulder != null && leftHip != null &&
            leftAnkle != null && leftFoot != null && leftKnee != null) {
            SquatPoints(nose, leftShoulder, leftHip, leftAnkle, leftFoot, leftKnee)
        } else null
    }

    private fun isFormCorrect(points: SquatPoints): Boolean {
        var isShoulderDifferenceBig = false
        if(landmarkDataManager.getLandmarkCount() != 0){
            val firstShoulderXFloat = landmarkDataManager.getFirstShoulderX()
            val currentShoulderXFloat = points.leftShoulder.x
            val shoulderXDifference = abs(currentShoulderXFloat - firstShoulderXFloat)
            isShoulderDifferenceBig = shoulderXDifference > 0.05f
            Log.d("SquatChecker", "shoulder difference: $shoulderXDifference")
        }

        val kneeOverFoot = points.leftKnee.x > points.leftFoot.x + 0.05f
        if (kneeOverFoot) {
            statusString = "Knee can't be over foot"
        }

        return !isShoulderDifferenceBig && !kneeOverFoot
    }

    private fun processExerciseState(landmarks: List<ConvertedLandmark>, points: SquatPoints) {
        val hipAngle = get2dAngleBetweenPoints(
            points.leftShoulder.toFloat2(),
            points.leftHip.toFloat2(),
            points.leftKnee.toFloat2()
        )

        val kneeAngle = get2dAngleBetweenPoints(
            points.leftHip.toFloat2(),
            points.leftKnee.toFloat2(),
            points.leftAnkle.toFloat2()
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
        val leftShoulder: ConvertedLandmark,
        val leftHip: ConvertedLandmark,
        val leftAnkle: ConvertedLandmark,
        val leftFoot: ConvertedLandmark,
        val leftKnee: ConvertedLandmark
    )
}