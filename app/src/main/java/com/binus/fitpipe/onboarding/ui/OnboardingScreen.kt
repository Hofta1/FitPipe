package com.binus.fitpipe.onboarding.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.binus.fitpipe.R
import com.binus.fitpipe.ui.theme.Black100
import com.binus.fitpipe.ui.theme.FadingCircle
import com.binus.fitpipe.ui.theme.FitPipeTheme
import com.binus.fitpipe.ui.theme.Typo
import com.binus.fitpipe.ui.theme.Yellow50
import com.example.yourapp.ui.components.FPScaffold

@Composable
internal fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onStart: () -> Unit = {},
) {
    FitPipeTheme {
        OnBoardingScreen(
            modifier = modifier,
            onStart = onStart
        )
    }
}


@Composable
private fun OnBoardingScreen(
    modifier: Modifier = Modifier,
    onStart: () -> Unit,
) {
    Box(modifier = modifier
        .fillMaxSize(),
    ){
        FadingCircle(
            height = 673,
            width = 581,
        )
        Column(
            modifier = modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Spacer(modifier.size(221.dp),)
            Image(
                painter = painterResource(R.drawable.pose_tracker_logo),
                contentDescription = "Pose Tracker Logo",
                alignment = Alignment.Center,
                modifier = modifier
                    .size(306.dp, 306.dp)
                    .weight(1f)
            )
//            Spacer(modifier.size(188.dp))
            OnboardingButton(
                modifier = Modifier
                    .weight(1f),
                text = "LET'S START",
                onStart = onStart,
            )
        }
    }

}

@Composable
private fun OnboardingButton(
    modifier: Modifier = Modifier,
    text: String,
    onStart: () -> Unit,
) {
    TextButton(
        modifier = modifier,
        onClick = onStart,
        content = {
            Box(
                modifier = Modifier
                    .background(color = Yellow50)
            ){
                Text(
                    text = text,
                    style = Typo.ExtraBoldTwentyFour,
                    color = Black100,
                    modifier = Modifier
                        .padding(vertical = 12.dp, horizontal = 58.dp)
                )
            }
        }
    )
}


@Preview
@Composable
private fun OnboardingScreenPreview() {
    FitPipeTheme {
        FPScaffold {
            OnBoardingScreen(
                onStart = {}
            )
        }
    }
}