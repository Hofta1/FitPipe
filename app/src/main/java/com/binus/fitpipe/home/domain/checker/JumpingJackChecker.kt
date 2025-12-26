package com.binus.fitpipe.home.domain.checker

import android.util.Log
import com.binus.fitpipe.home.domain.data.LandmarkDataManager
import com.binus.fitpipe.home.domain.state.ExerciseState
import com.binus.fitpipe.home.domain.state.ExerciseStateManager
import com.binus.fitpipe.home.domain.utils.AngleCalculator.get2dAngleBetweenPoints
import com.binus.fitpipe.home.domain.utils.AngleCalculator.get3dAngleBetweenPoints
import com.binus.fitpipe.home.domain.utils.AngleCalculator.isInTolerance
import com.binus.fitpipe.poselandmarker.ConvertedLandmark
import com.binus.fitpipe.poselandmarker.MediaPipeKeyPointEnum
import dev.romainguy.kotlin.math.Float2
import dev.romainguy.kotlin.math.Float3
import kotlin.math.abs

class JumpingJackChecker(
    private val landmarkDataManager: LandmarkDataManager,
    private val exerciseStateManager: ExerciseStateManager,
    private val onExerciseCompleted: (List<List<ConvertedLandmark>>) -> Unit,
    private val onUpdateStatusString: (String) -> Unit,
    private val onUpdateState: (ExerciseState) -> Unit
): ExerciseChecker() {
    fun checkExercise(convertedLandmarks: List<ConvertedLandmark>): Boolean {
        val points = extractRequiredPoints(convertedLandmarks) ?: return false
        val leftElbowAngle = get2dAngleBetweenPoints(
            points.leftShoulder.toFloat2(),
            points.leftElbow.toFloat2(),
            points.leftWrist.toFloat2()
        )
        val rightElbowAngle = get2dAngleBetweenPoints(
            points.rightShoulder.toFloat2(),
            points.rightElbow.toFloat2(),
            points.rightWrist.toFloat2()
        )

        val leftKneeAngle = get2dAngleBetweenPoints(
            points.leftHip.toFloat2(),
            points.leftKnee.toFloat2(),
            points.leftAnkle.toFloat2()
        )
        val rightKneeAngle = get2dAngleBetweenPoints(
            points.rightHip.toFloat2(),
            points.rightKnee.toFloat2(),
            points.rightAnkle.toFloat2()
        )

        val leftArmAngle = get2dAngleBetweenPoints(
            points.leftWrist.toFloat2(),
            points.leftShoulder.toFloat2(),
            points.leftHip.toFloat2()
        )
        val rightArmAngle = get2dAngleBetweenPoints(
            points.rightWrist.toFloat2(),
            points.rightShoulder.toFloat2(),
            points.rightHip.toFloat2()
        )

        val leftHipAngle = get2dAngleBetweenPoints(
            points.leftAnkle.toFloat2(),
            points.middleHip.toFloat2(),
            Float2(points.middleHip.x, points.leftAnkle.y)
        )

        val rightHipAngle = get2dAngleBetweenPoints(
            points.rightAnkle.toFloat2(),
            points.middleHip.toFloat2(),
            Float2(points.middleHip.x, points.rightAnkle.y)
        )

        if(exerciseStateManager.getCurrentState() == ExerciseState.EXERCISE_FAILED){
            handleFailed()
        }

        if (!isFormCorrect(
            leftKneeAngle,
            rightKneeAngle,
            leftArmAngle,
            rightArmAngle,
            leftHipAngle,
            rightHipAngle
        )) {
            isFormOkay = false
            badFormFrameCount++
            return false
        }

        isFormOkay = true
        processExerciseState(
            convertedLandmarks,
            leftArmAngle,
            rightArmAngle,
            leftHipAngle,
            rightHipAngle
            )
        if (exerciseStateManager.getCurrentState() != ExerciseState.WAITING_TO_START && badFormFrameCount >= BAD_FORM_THRESHOLD) {
            exerciseStateManager.updateState(ExerciseState.EXERCISE_FAILED)
        }
        return true
    }

    private fun extractRequiredPoints(landmarks: List<ConvertedLandmark>): JumpingJackPoints? {
        val nose = landmarks[MediaPipeKeyPointEnum.NOSE.keyId].takeIf { it.visibility.get() > 0.75f } ?: return null
        val leftShoulder = landmarks[MediaPipeKeyPointEnum.LEFT_SHOULDER.keyId].takeIf { it.visibility.get() > 0.75f } ?: return null
        val rightShoulder = landmarks[MediaPipeKeyPointEnum.RIGHT_SHOULDER.keyId].takeIf { it.visibility.get() > 0.75f } ?: return null
        val leftElbow = landmarks[MediaPipeKeyPointEnum.LEFT_ELBOW.keyId].takeIf { it.visibility.get() > 0.75f } ?: return null
        val rightElbow = landmarks[MediaPipeKeyPointEnum.RIGHT_ELBOW.keyId].takeIf { it.visibility.get() > 0.75f } ?: return null
        val leftWrist = landmarks[MediaPipeKeyPointEnum.LEFT_WRIST.keyId].takeIf { it.visibility.get() > 0.75f } ?: return null
        val rightWrist = landmarks[MediaPipeKeyPointEnum.RIGHT_WRIST.keyId].takeIf { it.visibility.get() > 0.75f } ?: return null
        val leftHip = landmarks[MediaPipeKeyPointEnum.LEFT_HIP.keyId].takeIf { it.visibility.get() > 0.75f } ?: return null
        val rightHip = landmarks[MediaPipeKeyPointEnum.RIGHT_HIP.keyId].takeIf { it.visibility.get() > 0.75f } ?: return null
        val middleHip = landmarkDataManager.getMiddlePointOfLandmark(leftHip, rightHip)
        val leftKnee = landmarks[MediaPipeKeyPointEnum.LEFT_KNEE.keyId].takeIf { it.visibility.get() > 0.75f } ?: return null
        val rightKnee = landmarks[MediaPipeKeyPointEnum.RIGHT_KNEE.keyId].takeIf { it.visibility.get() > 0.75f } ?: return null
        val leftAnkle = landmarks[MediaPipeKeyPointEnum.LEFT_ANKLE.keyId].takeIf { it.visibility.get() > 0.75f } ?: return null
        val rightAnkle = landmarks[MediaPipeKeyPointEnum.RIGHT_ANKLE.keyId].takeIf { it.visibility.get() > 0.75f } ?: return null

        return JumpingJackPoints(
            nose = nose,
            leftShoulder = leftShoulder,
            rightShoulder = rightShoulder,
            leftElbow = leftElbow,
            rightElbow = rightElbow,
            leftWrist = leftWrist,
            rightWrist = rightWrist,
            leftHip = leftHip,
            rightHip = rightHip,
            middleHip = middleHip,
            leftKnee = leftKnee,
            rightKnee = rightKnee,
            leftAnkle = leftAnkle,
            rightAnkle = rightAnkle
        )

    }

    private fun isFormCorrect(
        leftKneeAngle: Float,
        rightKneeAngle: Float,
        leftShoulderAngle: Float,
        rightShoulderAngle: Float,
        leftHipAngle: Float,
        rightHipAngle: Float
    ): Boolean {

        val isLeftKneeStraight = leftKneeAngle.isInTolerance(150f, tolerance = 60f)
        val isRightKneeStraight = rightKneeAngle.isInTolerance(150f, tolerance = 60f)

        val isArmSymmetric = abs(leftShoulderAngle - rightShoulderAngle) < 30f
        val isLegSymmetric = abs(leftHipAngle - rightHipAngle) < 20f

        if(!isArmSymmetric){
            statusString = "Arms are not symmetric"
        } else if(!isLegSymmetric){
            statusString = "Legs are not symmetric"
        }

        return isLeftKneeStraight && isRightKneeStraight && isArmSymmetric && isLegSymmetric
    }

    private fun processExerciseState(
        landmarks: List<ConvertedLandmark>,
        leftArmAngle: Float,
        rightArmAngle: Float,
        leftHipAngle: Float,
        rightHipAngle: Float
    ) {

        when (exerciseStateManager.getCurrentState()) {
            ExerciseState.WAITING_TO_START -> checkStartingPosition(landmarks, leftArmAngle, rightArmAngle, leftHipAngle, rightHipAngle)
            ExerciseState.STARTED -> checkGoingFlex(landmarks, leftArmAngle, rightArmAngle, leftHipAngle, rightHipAngle)
            ExerciseState.GOING_FLEXION -> checkFlexMax(landmarks, leftArmAngle, rightArmAngle, leftHipAngle, rightHipAngle)
            ExerciseState.GOING_EXTENSION -> checkDepressionMax(landmarks, leftArmAngle, rightArmAngle, leftHipAngle, rightHipAngle)
            ExerciseState.EXERCISE_COMPLETED -> handleCompleted()
            ExerciseState.EXERCISE_FAILED -> handleFailed()
        }
    }

    private fun checkStartingPosition(
            landmarks: List<ConvertedLandmark>,
            leftArmAngle: Float,
            rightArmAngle: Float,
            leftHipAngle: Float,
            rightHipAngle: Float,
        ) {
        val isArmsDown = leftArmAngle < 35f && rightArmAngle < 35f
        val isLegsTogether = leftHipAngle < 20f && rightHipAngle < 20f
        onUpdateState(exerciseStateManager.getCurrentState())
        if (isArmsDown && isLegsTogether) {
            Log.d("JumpingJackChecker", "Start")
            exerciseStateManager.updateState(ExerciseState.STARTED)
            landmarkDataManager.addLandmarks(landmarks)
        }
    }

    private fun checkGoingFlex(
        landmarks: List<ConvertedLandmark>,
        leftArmAngle: Float,
        rightArmAngle: Float,
        leftHipAngle: Float,
        rightHipAngle: Float,
    ) {
        val leftArmAngleDifference = abs(leftArmAngle - landmarkDataManager.getLastLeftArmAngle())
        val rightArmAngleDifference = abs(rightArmAngle - landmarkDataManager.getLastRightArmAngle())
        val leftHipAngleDifference = abs(leftHipAngle - landmarkDataManager.getLastLeftHipAngle())
        val rightHipAngleDifference = abs(rightHipAngle - landmarkDataManager.getLastRightHipAngle())
        val isAngleDifferenceBigEnough = leftArmAngleDifference > 5f && rightArmAngleDifference > 5f &&
                leftHipAngleDifference > 5f && rightHipAngleDifference > 5f
        if (isAngleDifferenceBigEnough) {
            Log.d("JumpingJackChecker", "Flex")
            landmarkDataManager.addLandmarks(landmarks)
            exerciseStateManager.updateState(ExerciseState.GOING_FLEXION)
            onUpdateState(exerciseStateManager.getCurrentState())
        }
    }

    private fun checkFlexMax(
        landmarks: List<ConvertedLandmark>,
        leftArmAngle: Float,
        rightArmAngle: Float,
        leftHipAngle: Float,
        rightHipAngle: Float,
    ) {
        if (leftArmAngle >= landmarkDataManager.getLastLeftArmAngle() ||
            rightArmAngle >= landmarkDataManager.getLastRightArmAngle() ||
            leftHipAngle >= landmarkDataManager.getLastLeftHipAngle() ||
            rightHipAngle >= landmarkDataManager.getLastRightHipAngle()) {
            if (leftArmAngle > 150f && rightArmAngle > 150f &&
                (leftHipAngle + rightHipAngle).isInTolerance(75f, tolerance = 36f)) {
                Log.d("JumpingJackChecker", "Extend")
                exerciseStateManager.updateState(ExerciseState.GOING_EXTENSION)
                onUpdateState(exerciseStateManager.getCurrentState())
            }
            landmarkDataManager.addLandmarks(landmarks)
        }else if(landmarkDataManager.getLastLeftHipAngle() - leftHipAngle > 10f ||
            landmarkDataManager.getLastRightHipAngle() - rightHipAngle > 10f){
            badFormFrameCount++
            statusString = "Did not reach maximum flexion"
        }
    }

    private fun checkDepressionMax(
        landmarks: List<ConvertedLandmark>,
        leftArmAngle: Float,
        rightArmAngle: Float,
        leftHipAngle: Float,
        rightHipAngle: Float,
    ) {

        val isArmsDown = leftArmAngle < 60f && rightArmAngle < 60f
        val isLegsTogether = leftHipAngle < 40f && rightHipAngle < 40f
        if (leftArmAngle <= landmarkDataManager.getLastLeftArmAngle() ||
            rightArmAngle <= landmarkDataManager.getLastRightArmAngle() ||
            leftHipAngle <= landmarkDataManager.getLastLeftHipAngle() ||
            rightHipAngle <= landmarkDataManager.getLastRightHipAngle()) {
            landmarkDataManager.addLandmarks(landmarks)
            if (isArmsDown && isLegsTogether) {
                Log.d("JumpingJackChecker", "Complete")
                exerciseStateManager.updateState(ExerciseState.EXERCISE_COMPLETED)
            }
        } else if ( leftArmAngle - landmarkDataManager.getLastLeftArmAngle() > 10f &&
            rightArmAngle - landmarkDataManager.getLastRightArmAngle() > 10f &&
            leftHipAngle - landmarkDataManager.getLastLeftHipAngle() > 10f &&
            rightHipAngle - landmarkDataManager.getLastRightHipAngle() > 10f) {
            exerciseStateManager.updateState(ExerciseState.EXERCISE_FAILED)
            statusString = "Did not reach maximum depression"
        }
    }

    private fun handleCompleted() {
        Log.d("JumpingJackChecker", "Completed sendinh")
        if (landmarkDataManager.getLandmarkCount() >= 60) {
            exerciseStateManager.updateState(ExerciseState.EXERCISE_FAILED)
        }else{
            onExerciseCompleted(landmarkDataManager.getAllLandmarks())
        }
        landmarkDataManager.clear()
        exerciseStateManager.reset()
        badFormFrameCount = 0
    }

    private fun handleFailed() {
        landmarkDataManager.clear()
        onUpdateStatusString(statusString)
        exerciseStateManager.reset()
        badFormFrameCount = 0
    }

    private data class JumpingJackPoints(
        val nose: ConvertedLandmark,
        val leftShoulder: ConvertedLandmark,
        val rightShoulder: ConvertedLandmark,
        val leftElbow: ConvertedLandmark,
        val rightElbow: ConvertedLandmark,
        val leftWrist: ConvertedLandmark,
        val rightWrist: ConvertedLandmark,
        val leftHip: ConvertedLandmark,
        val rightHip: ConvertedLandmark,
        val middleHip: ConvertedLandmark,
        val leftKnee: ConvertedLandmark,
        val rightKnee: ConvertedLandmark,
        val leftAnkle: ConvertedLandmark,
        val rightAnkle: ConvertedLandmark,
    )
}