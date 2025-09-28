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
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.dot
import dev.romainguy.kotlin.math.length
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.math.acos

@HiltViewModel
class HomeViewModel
@Inject
constructor(
    private val homeRepository: HomeRepository,
) : ViewModel() {
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

    fun sendLandmarkData(
        exerciseTitle: String,
        convertedLandmarks: List<ConvertedLandmark>,
    ) {
        val shouldSendLandmark = isImportantKeypointPresent(exerciseTitle, convertedLandmarks)
        if (!shouldSendLandmark) {
            Log.d("HomeViewModel", "Important keypoints missing, not sending data.")
            return
        }
        Log.d("HomeViewModel", "Important keypoints present, sending data.")

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
        p1: Float3,
        p2: Float3,
        p3: Float3,
    ): Float{
        val v1 = p1 - p2
        val v2 = p3 - p2
        val dotProduct = dot(v1, v2)
        val magnitudeV1 = length(v1)
        val magnitudeV2 = length(v2)
        val angleInRadians = acos(dotProduct / (magnitudeV1 * magnitudeV2))
        val angleInDegrees = Math.toDegrees(angleInRadians.toDouble()).toFloat()
        Log.d("HomeViewModel", "Angle: $angleInDegrees")
        return angleInDegrees
    }
}
