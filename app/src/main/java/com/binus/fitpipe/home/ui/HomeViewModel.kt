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
            ExerciseKey.situp -> {
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
                            exerciseKey = ExerciseKey.situp,
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
        }
        checkKeyPointsAngles(convertedLandmarks)
    }

    private fun checkKeyPointsAngles(
        convertedLandmarks: List<ConvertedLandmark>,
    ) {
        val exerciseTitle = currentExerciseTitle
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

            ExerciseKey.situp -> {
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


    private fun convertTitleToKey(title: String): ExerciseKey? {
        return when (title) {
            "Push Up" -> ExerciseKey.push_up
            "Sit Up" -> ExerciseKey.situp
            "Jumping Jack" -> ExerciseKey.jumping_jack
            "Squat" -> ExerciseKey.squat
            else -> null
        }
    }
}
