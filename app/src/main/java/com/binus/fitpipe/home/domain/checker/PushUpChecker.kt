package com.binus.fitpipe.home.domain.checker

import android.util.Log
import com.binus.fitpipe.home.domain.data.LandmarkDataManager
import com.binus.fitpipe.home.domain.state.ExerciseState
import com.binus.fitpipe.home.domain.state.ExerciseStateManager
import com.binus.fitpipe.home.domain.utils.AngleCalculator.get3dAngleBetweenPoints
import com.binus.fitpipe.home.domain.utils.AngleCalculator.getAngleBetweenPoints
import com.binus.fitpipe.home.domain.utils.AngleCalculator.isInTolerance
import com.binus.fitpipe.poselandmarker.ConvertedLandmark
import com.binus.fitpipe.poselandmarker.MediaPipeKeyPointEnum
import dev.romainguy.kotlin.math.Float2
import dev.romainguy.kotlin.math.Float3
import kotlin.math.abs

class PushUpChecker(
    private val landmarkDataManager: LandmarkDataManager,
    private val exerciseStateManager: ExerciseStateManager,
    private val onExerciseCompleted: (List<List<ConvertedLandmark>>) -> Unit
) {
    var isFormOkay = false
    var statusString = ""
    private var badFormFrameCount = 0
    private val BAD_FORM_THRESHOLD = 12

    fun getFormattedStatus(): String {
        return statusString
    }

    fun getFormStatus(): Boolean {
        return isFormOkay
    }

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
        val leftEye = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_EYE }
        val leftShoulder = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_SHOULDER }
        val leftHip = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_HIP }
        val leftAnkle = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_ANKLE }
        val leftWrist = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_WRIST }
        val leftElbow = landmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_ELBOW }

        return if (nose != null && leftEye != null && leftShoulder != null && leftHip != null &&
            leftAnkle != null && leftWrist != null && leftElbow != null) {
            PushUpPoints(nose, leftEye, leftShoulder, leftHip, leftAnkle, leftWrist, leftElbow)
        } else null
    }

    private fun isFormCorrect(points: PushUpPoints): Boolean {
        val neckAngle = get3dAngleBetweenPoints(
            points.leftEye.toFloat3(),
            points.leftShoulder.toFloat3(),
            points.leftHip.toFloat3()
        )
        val hipAngle = get3dAngleBetweenPoints(
            points.leftShoulder.toFloat3(),
            points.leftHip.toFloat3(),
            points.leftAnkle.toFloat3()
        )
        val bodyAngle = get3dAngleBetweenPoints(
            points.leftShoulder.toFloat3(),
            points.leftAnkle.toFloat3(),
            Float3(points.leftWrist.x, points.leftAnkle.y, points.leftWrist.z)
        )

        val bodyNotTooLow = points.leftEye.y > points.leftWrist.y - 0.1f
        val isBodyStraight = neckAngle > 120f && neckAngle < 185f && hipAngle.isInTolerance(165f)
        val isBodyAngleOkay = bodyAngle < 60f

        Log.d("PushUpCheckerStart", "Neck Angle: $neckAngle, Hip Angle: $hipAngle, Body Angle: $bodyAngle")
        Log.d("PushUpCheckerStart", "Body Not Too Low: $bodyNotTooLow, Is Body Straight: $isBodyStraight, Is Body Angle Okay: $isBodyAngleOkay")

        return isBodyStraight && isBodyAngleOkay && bodyNotTooLow
    }

    private fun processExerciseState(landmarks: List<ConvertedLandmark>, points: PushUpPoints) {
        val elbowAngle = get3dAngleBetweenPoints(
            points.leftShoulder.toFloat3(),
            points.leftElbow.toFloat3(),
            points.leftWrist.toFloat3()
        )
        val armAngle = get3dAngleBetweenPoints(
            points.leftWrist.toFloat3(),
            points.leftShoulder.toFloat3(),
            points.leftHip.toFloat3()
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

        Log.d("PushUpCheckerGood", "Elbow Angle: $elbowAngle, Arm Angle: $armAngle")
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
        val leftEye: ConvertedLandmark,
        val leftShoulder: ConvertedLandmark,
        val leftHip: ConvertedLandmark,
        val leftAnkle: ConvertedLandmark,
        val leftWrist: ConvertedLandmark,
        val leftElbow: ConvertedLandmark
    )
}