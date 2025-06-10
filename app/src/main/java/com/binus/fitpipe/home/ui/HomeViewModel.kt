package com.binus.fitpipe.home.ui

import androidx.lifecycle.ViewModel
import com.binus.fitpipe.home.data.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    // Add any properties or methods needed for the HomeViewModel here

    init {
        // Initialization logic if needed
    }
}