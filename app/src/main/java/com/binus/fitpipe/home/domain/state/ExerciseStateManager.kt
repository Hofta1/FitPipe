package com.binus.fitpipe.home.domain.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ExerciseState {
    WAITING_TO_START,
    STARTED,
    GOING_FLEXION,
    GOING_EXTENSION,
    EXERCISE_COMPLETED,
    EXERCISE_FAILED,
}
class ExerciseStateManager {
    private val _exerciseState = MutableStateFlow(ExerciseState.WAITING_TO_START)
    val exerciseState: StateFlow<ExerciseState> = _exerciseState.asStateFlow()

    fun updateState(newState: ExerciseState) {
        _exerciseState.update { newState }
    }

    fun getCurrentState(): ExerciseState = exerciseState.value

    fun reset() {
        _exerciseState.update { ExerciseState.WAITING_TO_START }
    }
}