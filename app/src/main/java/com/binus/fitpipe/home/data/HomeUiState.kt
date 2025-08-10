package com.binus.fitpipe.home.data

import com.binus.fitpipe.ExerciseKey

data class HomeUiState(
    val isLoading: Boolean = true,
    val rows: List<HomeRowData> = emptyList(),
    val scanResponse: String = "",
)

data class HomeRowData(
    val title: String,
    val imageResourceId: Int,
    val exerciseKey: ExerciseKey,
)
