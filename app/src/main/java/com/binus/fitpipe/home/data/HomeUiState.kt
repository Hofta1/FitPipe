package com.binus.fitpipe.home.data

import com.binus.fitpipe.ExerciseKey

data class HomeUiState(
    val isLoading: Boolean = true,
    val rows: List<HomeRowData> = emptyList(),
    val exerciseCount: Int = 0,
    val formattedStatusString: String = "",
    val isFormOkay: Boolean = false,
    val isUseAPIStatus: Boolean = false,
)

data class HomeRowData(
    val title: String,
    val imageResourceId: Int,
    val exerciseKey: ExerciseKey,
)

data class FeedbackLogUI(
    val fullFeedback: String,
    val status: Boolean
)