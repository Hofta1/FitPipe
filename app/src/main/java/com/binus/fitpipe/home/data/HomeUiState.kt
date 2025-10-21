package com.binus.fitpipe.home.data

import com.binus.fitpipe.ExerciseKey

data class HomeUiState(
    val isLoading: Boolean = true,
    val rows: List<HomeRowData> = emptyList(),
    val exerciseCount: Int = 0,
    val isImportantKeypointPresent: Boolean = false,
    val formattedStatusString: String = "",
    val fullErrorMessage: MutableList<String> = mutableListOf(),
    val isFormOkay: Boolean = true,
)

data class HomeRowData(
    val title: String,
    val imageResourceId: Int,
    val exerciseKey: ExerciseKey,
)
