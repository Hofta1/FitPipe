package com.binus.fitpipe.home.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.binus.fitpipe.ExerciseKey
import com.binus.fitpipe.R
import com.binus.fitpipe.home.data.HomeRepository
import com.binus.fitpipe.home.data.HomeRowData
import com.binus.fitpipe.home.data.HomeUiState
import com.binus.fitpipe.poselandmarker.ConvertedLandmark
import com.binus.fitpipe.poselandmarker.MediaPipeKeyPointEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.romainguy.kotlin.math.Float2
import dev.romainguy.kotlin.math.dot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val homeRepository: HomeRepository,
    ) : ViewModel() {
        private val listOfLandmarkList: MutableList<List<ConvertedLandmark>> = mutableListOf()
        private val angleTolerance = 20f

        private val exerciseState: MutableStateFlow<ExerciseState> =
            MutableStateFlow(ExerciseState.WAITING_TO_START)
        private val importantPushUpKeyPoints =
            listOf(
                MediaPipeKeyPointEnum.NOSE,
                MediaPipeKeyPointEnum.LEFT_SHOULDER,
                MediaPipeKeyPointEnum.RIGHT_SHOULDER,
                MediaPipeKeyPointEnum.LEFT_ELBOW,
                MediaPipeKeyPointEnum.RIGHT_ELBOW,
                MediaPipeKeyPointEnum.LEFT_WRIST,
                MediaPipeKeyPointEnum.RIGHT_WRIST,
                MediaPipeKeyPointEnum.LEFT_HIP,
                MediaPipeKeyPointEnum.RIGHT_HIP,
                MediaPipeKeyPointEnum.LEFT_KNEE,
                MediaPipeKeyPointEnum.RIGHT_KNEE,
                MediaPipeKeyPointEnum.LEFT_ANKLE,
                MediaPipeKeyPointEnum.RIGHT_ANKLE,
            )
        private val importantSitUpKeyPoints =
            listOf(
                MediaPipeKeyPointEnum.LEFT_SHOULDER,
                MediaPipeKeyPointEnum.RIGHT_SHOULDER,
                MediaPipeKeyPointEnum.LEFT_ELBOW,
                MediaPipeKeyPointEnum.RIGHT_ELBOW,
                MediaPipeKeyPointEnum.LEFT_HIP,
                MediaPipeKeyPointEnum.RIGHT_HIP,
                MediaPipeKeyPointEnum.LEFT_KNEE,
                MediaPipeKeyPointEnum.RIGHT_KNEE,
                MediaPipeKeyPointEnum.LEFT_ANKLE,
                MediaPipeKeyPointEnum.RIGHT_ANKLE,
            )
        private val importantJumpingJackKeyPoints =
            listOf(
                MediaPipeKeyPointEnum.NOSE,
                MediaPipeKeyPointEnum.LEFT_SHOULDER,
                MediaPipeKeyPointEnum.RIGHT_SHOULDER,
                MediaPipeKeyPointEnum.LEFT_ELBOW,
                MediaPipeKeyPointEnum.RIGHT_ELBOW,
                MediaPipeKeyPointEnum.LEFT_WRIST,
                MediaPipeKeyPointEnum.RIGHT_WRIST,
                MediaPipeKeyPointEnum.LEFT_HIP,
                MediaPipeKeyPointEnum.RIGHT_HIP,
                MediaPipeKeyPointEnum.LEFT_KNEE,
                MediaPipeKeyPointEnum.RIGHT_KNEE,
                MediaPipeKeyPointEnum.LEFT_ANKLE,
                MediaPipeKeyPointEnum.RIGHT_ANKLE,
            )
        private val importantSquatKeyPoints =
            listOf(
                MediaPipeKeyPointEnum.NOSE,
                MediaPipeKeyPointEnum.LEFT_SHOULDER,
                MediaPipeKeyPointEnum.RIGHT_SHOULDER,
                MediaPipeKeyPointEnum.LEFT_HIP,
                MediaPipeKeyPointEnum.RIGHT_HIP,
                MediaPipeKeyPointEnum.LEFT_KNEE,
                MediaPipeKeyPointEnum.RIGHT_KNEE,
                MediaPipeKeyPointEnum.LEFT_ANKLE,
                MediaPipeKeyPointEnum.RIGHT_ANKLE,
                MediaPipeKeyPointEnum.LEFT_FOOT_INDEX,
                MediaPipeKeyPointEnum.RIGHT_FOOT_INDEX,
            )
        private val _uiState = MutableStateFlow(HomeUiState())
        val uiState = _uiState.asStateFlow()

        init {
            loadInitialData()
        }

        private fun loadInitialData() {
            _uiState.update { currentState ->
                currentState.copy(isLoading = true)
                currentState.copy(
                    isLoading = false,
                    rows =
                        listOf(
                            HomeRowData(
                                title = "Push Up",
                                imageResourceId = R.drawable.push_up_icon,
                                exerciseKey = ExerciseKey.push_up,
                            ),
                            HomeRowData(
                                title = "Sit Up",
                                imageResourceId = R.drawable.sit_up_icon,
                                exerciseKey = ExerciseKey.sit_up,
                            ),
                            HomeRowData(
                                title = "Jumping Jack",
                                imageResourceId = R.drawable.jumping_jack_icon,
                                exerciseKey = ExerciseKey.jumping_jack,
                            ),
                            HomeRowData(
                                title = "Squat",
                                imageResourceId = R.drawable.squat_icon,
                                exerciseKey = ExerciseKey.squat,
                            ),
                        ),
                )
            }
        }

        fun saveLandmark(
            exerciseTitle: String,
            convertedLandmarks: List<ConvertedLandmark>,
        ) {
            val shouldSendLandmark = isImportantKeypointPresent(exerciseTitle, convertedLandmarks)
            if (!shouldSendLandmark) {
                Log.d("HomeViewModel", "Important keypoints missing, not sending data.")
                return
            }
            checkKeyPointsAngles(exerciseTitle, convertedLandmarks)
        }

        private fun checkKeyPointsAngles(
            exerciseTitle: String,
            convertedLandmarks: List<ConvertedLandmark>,
        ) {
            val exerciseKey = convertTitleToKey(exerciseTitle)
            when (exerciseKey) {
                ExerciseKey.push_up -> {
                    val nose =
                        convertedLandmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.NOSE }
                    val leftShoulder =
                        convertedLandmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_SHOULDER }
                    val leftHip =
                        convertedLandmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_HIP }
                    val leftAnkle =
                        convertedLandmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_ANKLE }
                    val leftFoot =
                        convertedLandmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_FOOT_INDEX }
                    val leftWrist =
                        convertedLandmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_WRIST }
                    val leftElbow =
                        convertedLandmarks.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_ELBOW }

                    if (nose != null &&
                        leftShoulder != null &&
                        leftHip != null &&
                        leftAnkle != null &&
                        leftFoot != null &&
                        leftWrist != null &&
                        leftElbow != null
                    ) {
                        val neckAngle =
                            getAngleBetweenPoints(
                                nose.toFloat2(),
                                leftShoulder.toFloat2(),
                                leftHip.toFloat2(),
                            )
                        val hipAngle =
                            getAngleBetweenPoints(
                                leftShoulder.toFloat2(),
                                leftHip.toFloat2(),
                                leftAnkle.toFloat2(),
                            )
                        val elbowAngle =
                            getAngleBetweenPoints(
                                leftShoulder.toFloat2(),
                                leftElbow.toFloat2(),
                                leftWrist.toFloat2(),
                            )
                        val armAngle =
                            getAngleBetweenPoints(
                                leftWrist.toFloat2(),
                                leftShoulder.toFloat2(),
                                leftHip.toFloat2(),
                            )
                        val bodyAngle =
                            getAngleBetweenPoints(
                                leftWrist.toFloat2(),
                                leftAnkle.toFloat2(),
                                Float2(leftWrist.x, leftAnkle.y),
                            )
                        val isBodyStraight = neckAngle.isInTolerance(180f) && hipAngle.isInTolerance(180f)
                        val isBodyAngleOkay = bodyAngle < 60f
                        if (isBodyStraight && isBodyAngleOkay) {
                            Log.d("HomeViewModel", "Push Up form is good, neck angle: $neckAngle, hip angle: $hipAngle")
                            when (exerciseState.value) {
                                ExerciseState.WAITING_TO_START -> {
                                    checkPushUpStartingKeyPoint(convertedLandmarks, elbowAngle, armAngle)
                                }

                                ExerciseState.STARTED -> {
                                    checkIfGoingDown(elbowAngle)
                                }
                                ExerciseState.GOING_DOWN -> {
                                    // Check if the user has gone down enough
                                    checkPushUpGoingDownKeyPoint(convertedLandmarks, elbowAngle)
                                }
                                ExerciseState.GOING_UP -> {
                                    checkPushUpGoingUpKeyPoint(convertedLandmarks, elbowAngle)
                                }
                                ExerciseState.EXERCISE_COMPLETED -> {
                                    // Exercise already completed, do nothing or reset if needed
                                    if (listOfLandmarkList.size < 60)
                                        {
                                            sendLandmarkData(exerciseTitle, listOfLandmarkList)
                                        } else {
                                        Log.d(
                                            "HomeViewModel",
                                            "Too many landmarks collected: ${listOfLandmarkList.size}, not sending data.",
                                        )
                                        exerciseState.update {
                                            ExerciseState.EXERCISE_FAILED
                                        }
                                    }
                                }

                                ExerciseState.EXERCISE_FAILED -> {
                                    listOfLandmarkList.clear()
                                    exerciseState.update { ExerciseState.WAITING_TO_START }
                                }
                            }
                        } else {
                            Log.d("HomeViewModel", "Push Up form is bad, neck angle: $neckAngle, hip angle: $hipAngle")
                        }
                    }
                }

                ExerciseKey.sit_up -> {
                    // Implement sit-up angle checks if needed
                }

                ExerciseKey.jumping_jack -> {
                    // Implement jumping jack angle checks if needed
                }

                ExerciseKey.squat -> {
                    // Implement squat angle checks if needed
                }

                ExerciseKey.pull_up -> TODO()
                null -> {}
            }
        }

        private fun checkPushUpStartingKeyPoint(
            convertedLandmarks: List<ConvertedLandmark>,
            elbowAngle: Float,
            armAngle: Float,
        ) {
            if (elbowAngle.isInTolerance(180f) && armAngle.isInTolerance(90f)) {
                exerciseState.update { ExerciseState.STARTED }
                listOfLandmarkList.add(convertedLandmarks)
                Log.d("HomeViewModel", "Push Up started")
            }
        }

        private fun checkIfGoingDown(elbowAngle: Float) {
            val angleDifference = abs(elbowAngle - getLastElbowAngle())
            if (elbowAngle < getLastElbowAngle() && angleDifference > 5f) {
                exerciseState.update { ExerciseState.GOING_DOWN }
                Log.d("HomeViewModel", "Push Up going down")
            }
        }

        private fun checkPushUpGoingDownKeyPoint(
            convertedLandmarks: List<ConvertedLandmark>,
            elbowAngle: Float,
        ) {
            if (elbowAngle <= getLastElbowAngle())
                {
                    if (elbowAngle.isInTolerance(90f)) {
                        exerciseState.update { ExerciseState.GOING_UP }
                        Log.d("HomeViewModel", "Push Up going up")
                    }
                    listOfLandmarkList.add(convertedLandmarks)
                }
        }

        private fun checkPushUpGoingUpKeyPoint(
            convertedLandmarks: List<ConvertedLandmark>,
            elbowAngle: Float,
        ) {
            val isElbowAngleOkay = elbowAngle > 80f
            if (elbowAngle >= getLastElbowAngle())
                {
                    if (elbowAngle.isInTolerance(180f)) {
                        exerciseState.update { ExerciseState.EXERCISE_COMPLETED }
                        Log.d("HomeViewModel", "Push Up completed")
                    }
                    listOfLandmarkList.add(convertedLandmarks)
                } else if (!isElbowAngleOkay)
                {
                    exerciseState.update { ExerciseState.EXERCISE_FAILED }
                    Log.d("HomeViewModel", "Push Up failed")
                }
        }

        private fun getLastElbowAngle(): Float  {
            val lastLandmarkList = listOfLandmarkList.lastOrNull()
            val lastShoulder = lastLandmarkList?.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_SHOULDER }
            val lastElbow = lastLandmarkList?.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_ELBOW }
            val lastWrist = lastLandmarkList?.find { it.keyPointEnum == MediaPipeKeyPointEnum.LEFT_WRIST }
            return getAngleBetweenPoints(
                lastShoulder!!.toFloat2(),
                lastElbow!!.toFloat2(),
                lastWrist!!.toFloat2(),
            )
        }

        fun sendLandmarkData(
            exerciseTitle: String,
            convertedLandmarks: List<List<ConvertedLandmark>>,
        ) {
//
//            val landmarkFloatSequence = convertLandmarkToFloatSequence(convertedLandmark)
//            val exerciseKey = convertTitleToKey(exerciseTitle)
//            viewModelScope.launch {
//                val result = homeRepository.sendPoseLandmark(ConvertedLandmarkList(exerciseKey.toString(), landmarkFloatSequence))
//                result.onSuccess {
//                    // Handle success, e.g., show a success message or update UI
//                    val data = result.getOrNull()
//                    Log.d("HomeViewModel", "Pose landmark sent successfully: $data")
//                }.onFailure { exception ->
//                    // Handle failure, e.g., show an error message
//                    Log.d("HomeViewModel", "Failed to send pose landmark: ${exception.message}")
//                }
//            }
        }

        private fun convertLandmarkToFloatSequence(landmarks: List<ConvertedLandmark>): List<Float> {
            val floatSequence = mutableListOf<Float>()
            landmarks.forEach { landmark ->
                floatSequence.add(landmark.x)
                floatSequence.add(landmark.y)
                floatSequence.add(landmark.z)
            }
            return floatSequence
        }

        /**
         * Converts the exercise title to an ExerciseKey.
         * Returns null if the title does not match any known exercise.
         */
        private fun isImportantKeypointPresent(
            exerciseTitle: String,
            landmarks: List<ConvertedLandmark>,
        ): Boolean {
            val exerciseKey = convertTitleToKey(exerciseTitle)
            val importantKeyPoints =
                when (exerciseKey) {
                    ExerciseKey.push_up -> importantPushUpKeyPoints
                    ExerciseKey.sit_up -> importantSitUpKeyPoints
                    ExerciseKey.jumping_jack -> importantJumpingJackKeyPoints
                    ExerciseKey.squat -> importantSquatKeyPoints
                    else -> return false // Unknown exercise
                }
            val lowPresenceLandmarks = landmarks.filter { it.presence.get() < 0.9f }
            val allImportantKeyPointsPresent =
                importantKeyPoints.all { keyPointEnum ->
                    lowPresenceLandmarks.none { it.keyPointEnum == keyPointEnum }
                }
            return allImportantKeyPointsPresent
        }

        private fun convertTitleToKey(title: String): ExerciseKey? {
            return when (title) {
                "Push Up" -> ExerciseKey.push_up
                "Sit Up" -> ExerciseKey.sit_up
                "Jumping Jack" -> ExerciseKey.jumping_jack
                "Squat" -> ExerciseKey.squat
                else -> null
            }
        }

        private fun getAngleBetweenPoints(
            p1: Float2,
            p2: Float2,
            p3: Float2,
        ): Float {
            val baX = p1.x - p2.x
            val baY = p1.y - p2.y
            val bcX = p3.x - p2.x
            val bcY = p3.y - p2.y

            // Dot product and magnitudes
            val dot = baX * bcX + baY * bcY
            val magBA = sqrt(baX * baX + baY * baY)
            val magBC = sqrt(bcX * bcX + bcY * bcY)

            if (magBA == 0f || magBC == 0f) return 0f // avoid division by zero

            // cos(theta)
            val cosTheta = (dot / (magBA * magBC)).coerceIn(-1f, 1f)

            // Convert to degrees
            return Math.toDegrees(acos(cosTheta).toDouble()).toFloat()
        }

        private fun Float.isInTolerance(idealAngle: Float): Boolean  {
            return (this >= idealAngle - angleTolerance / 2 && this <= idealAngle + angleTolerance / 2)
        }

        private enum class ExerciseState {
            WAITING_TO_START,
            STARTED,
            GOING_DOWN,
            GOING_UP,
            EXERCISE_COMPLETED,
            EXERCISE_FAILED,
        }
    }
