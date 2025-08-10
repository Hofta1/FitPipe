package com.binus.fitpipe.home.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binus.fitpipe.ExerciseKey
import com.binus.fitpipe.R
import com.binus.fitpipe.home.data.HomeRepository
import com.binus.fitpipe.home.data.HomeRowData
import com.binus.fitpipe.home.data.HomeUiState
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
            landmarkInSequence: List<Float>,
        ) {
            val exerciseKey = convertTitleToKey(exerciseTitle)
            viewModelScope.launch {
                homeRepository.sendPoseLandmark(ConvertedLandmarkList(exerciseKey.toString(), landmarkInSequence))
                    .onSuccess { data ->
                        // Handle success, e.g., show a success message or update UI
                        _uiState.update { currentState ->
                            currentState.copy(
                                scanResponse = data.feedback.angleFeedback,
                            )
                        }
                    }.onFailure { exception ->
                        // Handle failure, e.g., show an error message
                        Log.d("HomeViewModel", "Failed to send pose landmark: ${exception.message}")
                    }
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
