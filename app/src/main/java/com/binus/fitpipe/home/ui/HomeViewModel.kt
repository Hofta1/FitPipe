package com.binus.fitpipe.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binus.fitpipe.R
import com.binus.fitpipe.home.data.HomeRepository
import com.binus.fitpipe.home.data.HomeRowData
import com.binus.fitpipe.home.data.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init{
        loadInitialData()
    }

    private fun loadInitialData() {
        _uiState.update { currentState ->
            currentState.copy(isLoading = true)
            currentState.copy(
                isLoading = false,
                rows = listOf(
                    HomeRowData(
                        title = "Push Up",
                        imageResourceId = R.drawable.push_up_icon
                    ),
                    HomeRowData(
                        title = "Sit Up",
                        imageResourceId = R.drawable.sit_up_icon
                    ),
                    HomeRowData(
                        title = "Jumping Jack",
                        imageResourceId = R.drawable.jumping_jack_icon
                    ),
                    HomeRowData(
                        title = "Squat",
                        imageResourceId = R.drawable.squat_icon
                    ),
                )
            )
        }
    }
    // Add any properties or methods needed for the HomeViewModel here

    init {
        // Initialization logic if needed
    }
}