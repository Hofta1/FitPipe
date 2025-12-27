package com.binus.fitpipe.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binus.fitpipe.ExerciseKey
import com.binus.fitpipe.R
import com.binus.fitpipe.home.data.FeedbackLogUI
import com.binus.fitpipe.home.data.HomeRepository
import com.binus.fitpipe.home.data.HomeRowData
import com.binus.fitpipe.home.data.HomeUiState
import com.binus.fitpipe.home.domain.checker.JumpingJackChecker
import com.binus.fitpipe.home.domain.checker.PushUpChecker
import com.binus.fitpipe.home.domain.checker.SitUpChecker
import com.binus.fitpipe.home.domain.checker.SquatChecker
import com.binus.fitpipe.home.domain.data.LandmarkDataManager
import com.binus.fitpipe.home.domain.state.ExerciseState
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
    private lateinit var jumpingJackChecker: JumpingJackChecker
    private lateinit var squatChecker: SquatChecker
    private var currentExerciseTitle: String = ""

    val fullErrorMessages: MutableList<FeedbackLogUI> = mutableListOf()

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
                    onUpdateStatusString = { statusString ->
                        updateStatusString(statusString)
                    },
                    onUpdateState = { exerciseState ->
                        updateExerciseState(exerciseState)
                    }
                )
            }
            ExerciseKey.situp -> {
                sitUpChecker = SitUpChecker(
                    landmarkDataManager,
                    exerciseState,
                    onExerciseCompleted = { landmarks ->
                        sendLandmarkData(exerciseTitle, landmarks)
                    },
                    onUpdateStatusString = { statusString ->
                        updateStatusString(statusString)
                    },
                    onUpdateState = { exerciseState ->
                        updateExerciseState(exerciseState)
                    }
                )
            }

            ExerciseKey.jumping_jack -> {
                jumpingJackChecker = JumpingJackChecker(
                    landmarkDataManager,
                    exerciseState,
                    onExerciseCompleted = { landmarks ->
                        sendLandmarkData(exerciseTitle, landmarks)
                    },
                    onUpdateStatusString = { statusString ->
                        updateStatusString(statusString)
                    },
                    onUpdateState = { exerciseState ->
                        updateExerciseState(exerciseState)
                    }
                )
            }

            ExerciseKey.squat -> {
                squatChecker = SquatChecker(
                    landmarkDataManager,
                    exerciseState,
                    onExerciseCompleted = { landmarks ->
                        sendLandmarkData(exerciseTitle, landmarks)
                    },
                    onUpdateStatusString = { statusString ->
                        updateStatusString(statusString)
                    },
                    onUpdateState = { exerciseState ->
                        updateExerciseState(exerciseState)
                    }
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
                    if(!uiState.value.isUseAPIStatus){
                        currentState.copy(
                            formattedStatusString = pushUpChecker.getFormattedStatus(),
                            isFormOkay = pushUpChecker.getFormStatus(),
                        )
                    } else {
                        currentState.copy(
                            isFormOkay = pushUpChecker.getFormStatus(),
                        )
                    }
                }
            }

            ExerciseKey.situp -> {
                sitUpChecker.checkExercise(convertedLandmarks)
                _uiState.update { currentState ->
                    if(!uiState.value.isUseAPIStatus){
                        currentState.copy(
                            formattedStatusString = sitUpChecker.getFormattedStatus(),
                            isFormOkay = sitUpChecker.getFormStatus(),
                        )
                    } else {
                        currentState.copy(
                            isFormOkay = sitUpChecker.getFormStatus(),
                        )
                    }
                }
            }

            ExerciseKey.jumping_jack -> {
                jumpingJackChecker.checkExercise(convertedLandmarks)
                _uiState.update { currentState ->
                    if(!uiState.value.isUseAPIStatus){
                        currentState.copy(
                            formattedStatusString = jumpingJackChecker.getFormattedStatus(),
                            isFormOkay = jumpingJackChecker.getFormStatus(),
                        )
                    } else {
                        currentState.copy(
                            isFormOkay = jumpingJackChecker.getFormStatus(),
                        )
                    }
                }
                // Implement jumping jack angle checks if needed
            }

            ExerciseKey.squat -> {
                squatChecker.checkExercise(convertedLandmarks)
                _uiState.update { currentState ->
                    if(!uiState.value.isUseAPIStatus){
                        currentState.copy(
                            formattedStatusString = squatChecker.getFormattedStatus(),
                            isFormOkay = squatChecker.getFormStatus(),
                        )
                    } else {
                        currentState.copy(
                            isFormOkay = squatChecker.getFormStatus(),
                        )
                    }
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
            viewModelScope.launch {
                val floatLandmarkList = mutableListOf<List<Float>>()
                convertedLandmarks.forEach { convertedLandmark->
                    floatLandmarkList.add(convertLandmarkToFloatSequence(convertedLandmark))
                }
                val exerciseKey = convertTitleToKey(exerciseTitle)
                val result = homeRepository.sendPoseLandmark(
                    ConvertedLandmarkList(
                        exerciseKey.toString(),
                        floatLandmarkList
                    )
                )
                result.onSuccess {
                    // Handle success, e.g., show a success message or update UI
                    val data = result.getOrNull()
                    _uiState.update { currentState ->
                        if(data?.status == true){
                            currentState.copy(
                                formattedStatusString = data.formattedFeedback ?: "",
                                exerciseCount = currentState.exerciseCount + 1,
                                isUseAPIStatus = true
                            )
                        } else {
                            currentState.copy(
                                formattedStatusString = data?.formattedFeedback ?: "",
                                isUseAPIStatus = true
                            )
                        }
                    }
                    fullErrorMessages.add(
                            FeedbackLogUI(
                                data?.fullFeedback ?: "",
                                data?.status ?: false
                            )
                        )
                }.onFailure { exception ->
                }
            }
    }

    fun getStateDrawableInt(): Int{
        val exerciseKey = convertTitleToKey(currentExerciseTitle)
        return when(exerciseKey){
            ExerciseKey.push_up -> {
                when(exerciseState.getCurrentState()){
                    ExerciseState.GOING_FLEXION -> R.drawable.push_up_flex
                    ExerciseState.GOING_EXTENSION -> R.drawable.push_up_depress
                    else -> R.drawable.push_up_start
                }
            }
            ExerciseKey.situp -> {
                when(exerciseState.getCurrentState()){
                    ExerciseState.GOING_FLEXION -> R.drawable.situp_flex
                    ExerciseState.GOING_EXTENSION -> R.drawable.situp_depress
                    else -> R.drawable.situp_flex
                }
            }
            ExerciseKey.squat -> {
                when(exerciseState.getCurrentState()){
                    ExerciseState.GOING_FLEXION -> R.drawable.squat_flex
                    ExerciseState.GOING_EXTENSION -> R.drawable.squat_depress
                    else -> R.drawable.squat_starting
                }
            }

            else -> R.drawable.pose_tracker_logo
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

    private fun updateStatusString(statusString: String) {
        _uiState.update { currentState ->
            currentState.copy(
                formattedStatusString = statusString,
            )
        }
    }

    private fun updateExerciseState(exerciseState: ExerciseState) {
        _uiState.update { currentState ->
            currentState.copy(
                exerciseState = exerciseState,
            )
        }
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
