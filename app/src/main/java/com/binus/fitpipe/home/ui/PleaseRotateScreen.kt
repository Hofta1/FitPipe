package com.binus.fitpipe.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.binus.fitpipe.ui.theme.Black100
import com.binus.fitpipe.ui.theme.FitPipeTheme
import com.binus.fitpipe.ui.theme.Typo
import com.binus.fitpipe.ui.theme.White80

@Composable
internal fun PleaseRotateScreen(modifier: Modifier, exerciseTitle: String = "") {
    Box(
        modifier = modifier.fillMaxSize(),
    ){
        Box(
            modifier = modifier.matchParentSize()
                .background(Black100)
                .alpha(0.5f)
        )
        if(exerciseTitle.isNotEmpty()) {
            Text(
                text = exerciseTitle,
                style = Typo.BoldTwentyFour,
                color = White80,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.TopCenter)
                    .padding(top = 64.dp)
            )
        }
        Text(
            text = "Please turn on your auto rotate then rotate your device to landscape mode to the right side.",
            style = Typo.BoldTwentyFour,
            color = White80,
            modifier = modifier.align(Alignment.Center),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_4_XL)
@Composable
private fun PleaseRotatePreview() {
    FitPipeTheme {
        PleaseRotateScreen(
            modifier = Modifier,
            exerciseTitle = "Jumping Jacks"
        )
    }
}