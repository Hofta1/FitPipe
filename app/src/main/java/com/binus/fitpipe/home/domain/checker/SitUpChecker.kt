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
        if (badFormFrameCount >= BAD_FORM_THRESHOLD &&
            exerciseStateManager.getCurrentState() != ExerciseState.WAITING_TO_START) {
            exerciseStateManager.updateState(ExerciseState.EXERCISE_FAILED)
        }
        isFormOkay = true
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
                statusString = "Bad lighting"
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
            isUsingLeft = true
            SitUpPoints(
                nose = nose,
                leftShoulder = leftShoulder,
                leftHip = leftHip,
                leftAnkle = leftAnkle,
                leftFoot = leftFoot,
                leftKnee = leftKnee
            )
        } else{
            isUsingLeft = false
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
        val idealKneeAngle = 70f
        val kneeAngle = get3dAngleBetweenPoints(
            points.leftHip.toFloat3(),
            points.leftKnee.toFloat3(),
            points.leftAnkle.toFloat3()
        )
        val kneeAngleCorrect = kneeAngle.isInTolerance(idealKneeAngle, tolerance = 60f)
        Log.d("SitUpChecker", "Hipy: ${points.leftHip.y} Ankley: ${points.leftAnkle.y}")
        val isFeetOnTheGround = points.leftHip.y - points.leftAnkle.y < -0.025f

        var kneeAngleChanged = false

        if(landmarkDataManager.getLandmarkCount() > 0){
            kneeAngleChanged = abs(kneeAngle - landmarkDataManager.getFirstKneeAngle(isUsingLeft)) > 40f
        }

        if(!kneeAngleCorrect) {
            statusString = if(kneeAngle > idealKneeAngle) {
                "Knees Not Bent Enough"
            }else{
                "Knees Bent Too Much"
            }
        } else if(!isFeetOnTheGround) {
            statusString = "Feet Not On Ground"
        }

        if(kneeAngleChanged){
            statusString = "Feet move too much"
        }

        return kneeAngleCorrect && !kneeAngleChanged && !isFeetOnTheGround
    }

    private fun processExerciseState(landmarks: List<ConvertedLandmark>, points: SitUpPoints) {
        val hipAngle = get3dAngleBetweenPoints(
            points.leftShoulder.toFloat3(),
            points.leftHip.toFloat3(),
            points.leftKnee.toFloat3()
        )

        when (exerciseStateManager.getCurrentState()) {
            ExerciseState.WAITING_TO_START -> checkStartingPosition(landmarks, hipAngle)
            ExerciseState.STARTED -> checkGoingUp(landmarks, hipAngle, points.leftShoulder.y)
            ExerciseState.GOING_FLEXION -> checkUpMax(landmarks, hipAngle, points.leftShoulder.y)
            ExerciseState.GOING_EXTENSION -> checkDownMax(landmarks, hipAngle, points.leftShoulder.y)
            ExerciseState.EXERCISE_COMPLETED -> handleCompleted()
            ExerciseState.EXERCISE_FAILED -> handleFailed()
        }
    }

    private fun checkStartingPosition(landmarks: List<ConvertedLandmark>, hipAngle: Float) {
        onUpdateState(exerciseStateManager.getCurrentState())
        if (hipAngle.isInTolerance(130f, tolerance = 20f)) {
            exerciseStateManager.updateState(ExerciseState.STARTED)
            landmarkDataManager.addLandmarks(landmarks)
            Log.d("SitUpChecker", "Sit Up started")
        }
    }

    private fun checkGoingUp(landmarks: List<ConvertedLandmark>, hipAngle: Float, shoulderY: Float) {
        val enum = if(isUsingLeft) MediaPipeKeyPointEnum.LEFT_SHOULDER else MediaPipeKeyPointEnum.RIGHT_SHOULDER
        val angleDifference = abs(hipAngle - landmarkDataManager.getLastHipAngle(isUsingLeft))
        if (hipAngle < landmarkDataManager.getLastHipAngle(isUsingLeft) && angleDifference > 10f && shoulderY - landmarkDataManager.getLastY(enum) > 0.05f) {
            landmarkDataManager.addLandmarks(landmarks)
            exerciseStateManager.updateState(ExerciseState.GOING_FLEXION)
            onUpdateState(exerciseStateManager.getCurrentState())
            Log.d("SitUpChecker", "Sit Up Going Up")
        }
    }

    private fun checkUpMax(landmarks: List<ConvertedLandmark>, hipAngle: Float, shoulderY: Float) {
        val enum = if(isUsingLeft) MediaPipeKeyPointEnum.LEFT_SHOULDER else MediaPipeKeyPointEnum.RIGHT_SHOULDER
        if (hipAngle <= landmarkDataManager.getLastHipAngle(isUsingLeft) && shoulderY > landmarkDataManager.getLastY(enum) ) {
            if (hipAngle < 70f) {
                exerciseStateManager.updateState(ExerciseState.GOING_EXTENSION)
                onUpdateState(exerciseStateManager.getCurrentState())
                Log.d("SitUpChecker", "Sit Up going down")
            }
            landmarkDataManager.addLandmarks(landmarks)
        }else if(hipAngle > 130f){
            // User going back to starting position without completing the sit up
            exerciseStateManager.updateState(ExerciseState.EXERCISE_FAILED)
            statusString = "Did not complete the sit up"
        }
    }

    private fun checkDownMax(landmarks: List<ConvertedLandmark>, hipAngle: Float, shoulderY: Float) {
        val enum = if(isUsingLeft) MediaPipeKeyPointEnum.LEFT_SHOULDER else MediaPipeKeyPointEnum.RIGHT_SHOULDER
        val isShoulderDeclining = shoulderY < landmarkDataManager.getLastY(enum)
        landmarkDataManager.addLandmarks(landmarks)
        Log.d("SitUpChecker", "hipAngle: $hipAngle lastHipAngle ${landmarkDataManager.getLastHipAngle(isUsingLeft)} shoulderY: $shoulderY lastShoulderY: ${landmarkDataManager.getLastY(enum)}")
        if (hipAngle >= landmarkDataManager.getLastHipAngle(isUsingLeft) && isShoulderDeclining) {
            if (hipAngle.isInTolerance(130f, tolerance = 50f)) {
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
        badFormFrameCount = 0
    }

    private fun handleFailed() {
        landmarkDataManager.clear()
        onUpdateStatusString(statusString)
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