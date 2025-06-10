package com.binus.fitpipe.onboarding.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.binus.fitpipe.ui.theme.FitPipeTheme

@Composable
internal fun OnboardingScreen(
    onStart: () -> Unit,
) {
    FitPipeTheme {
        onBoardingScreen(
            modifier = Modifier,
            onStart = onStart
        )
    }
}


@Composable
private fun onBoardingScreen(
    modifier: Modifier = Modifier,
    onStart: () -> Unit,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ){

        }
    }
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    FitPipeTheme {
        onBoardingScreen(
            onStart = {}
        )
    }
}