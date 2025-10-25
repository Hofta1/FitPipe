package com.binus.fitpipe.home.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binus.fitpipe.ExerciseKey
import com.binus.fitpipe.R
import com.binus.fitpipe.home.data.HomeRepository
import com.binus.fitpipe.home.data.HomeRowData
import com.binus.fitpipe.home.data.HomeUiState
import com.binus.fitpipe.home.domain.checker.PushUpChecker
import com.binus.fitpipe.home.domain.checker.SitUpChecker
import com.binus.fitpipe.home.domain.checker.SquatChecker
import com.binus.fitpipe.home.domain.data.LandmarkDataManager
import com.binus.fitpipe.home.domain.state.ExerciseStateManager
import com.binus.fitpipe.poselandmarker.ConvertedLandmark
import com.binus.fitpipe.poselandmarker.ConvertedLandmarkList
import com.binus.fitpipe.poselandmarker.MediaPipeKeyPointEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
@Inject
constructor(
    private val homeRepository: HomeRepository,
) : ViewModel() {
    private val landmarkDataManager = LandmarkDataManager()

    private val exerciseState = ExerciseStateManager()

    private lateinit var pushUpChecker: PushUpChecker
    private lateinit var sitUpChecker: SitUpChecker
    private lateinit var jumpingJackChecker: PushUpChecker
    private lateinit var squatChecker: SquatChecker
    private var currentExerciseTitle: String = ""

    private var importantKeyPoints: List<MediaPipeKeyPointEnum> = emptyList()

    private fun initializeChecker(exerciseTitle: String) {
        val exerciseKey = convertTitleToKey(exerciseTitle)
        when (exerciseKey) {
            ExerciseKey.push_up -> {
                pushUpChecker = PushUpChecker(
                    landmarkDataManager,
                    exerciseState,
                    onExerciseCompleted = { landmarks ->
                        sendLandmarkData(exerciseTitle, landmarks)
                    },
                )
            }
            ExerciseKey.sit_up -> {
                sitUpChecker = SitUpChecker(
                    landmarkDataManager,
                    exerciseState,
                    onExerciseCompleted = { landmarks ->
                        sendLandmarkData(exerciseTitle, landmarks)
                    },
                )
            }

            ExerciseKey.jumping_jack -> {
                // Initialize JumpingJackChecker when implemented
            }

            ExerciseKey.squat -> {
                squatChecker = SquatChecker(
                    landmarkDataManager,
                    exerciseState,
                    onExerciseCompleted = { landmarks ->
                        sendLandmarkData(exerciseTitle, landmarks)
                    },
                )
                // Initialize SquatChecker when implemented
            }

            ExerciseKey.pull_up -> TODO()
            null -> {}
        }

    }
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
        if (currentExerciseTitle != exerciseTitle) {
            landmarkDataManager.clear()
            exerciseState.reset()
            initializeChecker(exerciseTitle)
            currentExerciseTitle = exerciseTitle
            setImportantKeyPoints()
        }

        val isImportantKeypointPresent = isImportantKeypointPresent(convertedLandmarks)
        _uiState.update { currentState ->
            currentState.copy(
                isImportantKeypointPresent = isImportantKeypointPresent,
                formattedStatusString = "All keypoints present",
            )
        }
        if (!isImportantKeypointPresent) {
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
                pushUpChecker.checkExercise(convertedLandmarks)
                _uiState.update { currentState ->
                    currentState.copy(
                        formattedStatusString = pushUpChecker.getFormattedStatus(),
                        isFormOkay = pushUpChecker.getFormStatus(),
                    )
                }
            }

            ExerciseKey.sit_up -> {
                sitUpChecker.checkExercise(convertedLandmarks)
                _uiState.update { currentState ->
                    currentState.copy(
                        formattedStatusString = sitUpChecker.getFormattedStatus(),
                        isFormOkay = sitUpChecker.getFormStatus(),
                    )
                }
            }

            ExerciseKey.jumping_jack -> {
                jumpingJackChecker
                // Implement jumping jack angle checks if needed
            }

            ExerciseKey.squat -> {
                squatChecker.checkExercise(convertedLandmarks)
                _uiState.update { currentState ->
                    currentState.copy(
                        formattedStatusString = squatChecker.getFormattedStatus(),
                        isFormOkay = squatChecker.getFormStatus(),
                    )
                }
                // Implement squat angle checks if needed
            }

            ExerciseKey.pull_up -> TODO()
            null -> {}
        }
    }


    fun sendLandmarkData(
        exerciseTitle: String,
        convertedLandmarks: List<List<ConvertedLandmark>>,
    ) {
        _uiState.update {
            currentState ->
            currentState.copy(exerciseCount = currentState.exerciseCount + 1)
        }
        val floatLandmarkList = mutableListOf<List<Float>>()
        convertedLandmarks.forEach { convertedLandmark->
            floatLandmarkList.add(convertLandmarkToFloatSequence(convertedLandmark))
        }
        Log.d("HomeViewModel", "Sending landmark data for $floatLandmarkList")
            val exerciseKey = convertTitleToKey(exerciseTitle)
            viewModelScope.launch {
                val result = homeRepository.sendPoseLandmark(
                    ConvertedLandmarkList(
                        exerciseKey.toString(),
                        floatLandmarkList
                    )
                )
                result.onSuccess {
                    // Handle success, e.g., show a success message or update UI
                    val data = result.getOrNull()
                    Log.d("HomeViewModel", "Pose landmark sent successfully: $data")
                }.onFailure { exception ->
                    // Handle failure, e.g., show an error message
                    Log.d("HomeViewModel", "Failed to send pose landmark: ${exception.message}")
                }
            }
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
        landmarks: List<ConvertedLandmark>,
    ): Boolean {
        val lowPresenceLandmarks = landmarks.filter { it.presence.get() < 0.9f }
        val allImportantKeyPointsPresent =
            importantKeyPoints.all { keyPointEnum ->
                lowPresenceLandmarks.none { it.keyPointEnum == keyPointEnum }
            }
        return allImportantKeyPointsPresent
    }

    private fun setImportantKeyPoints(){
        val exerciseKey = convertTitleToKey(currentExerciseTitle)
        importantKeyPoints =
            when (exerciseKey) {
                ExerciseKey.push_up -> importantPushUpKeyPoints
                ExerciseKey.sit_up -> importantSitUpKeyPoints
                ExerciseKey.jumping_jack -> importantJumpingJackKeyPoints
                ExerciseKey.squat -> importantSquatKeyPoints
                else -> emptyList()
            }
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
}
