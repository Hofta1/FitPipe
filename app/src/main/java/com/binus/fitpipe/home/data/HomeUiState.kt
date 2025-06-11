package com.binus.fitpipe.home.data

data class HomeUiState(
    val isLoading: Boolean = true,
    val rows: List<HomeRowData> = emptyList(),
)

data class HomeRowData(
    val title: String,
    val imageResourceId: Int,
)
