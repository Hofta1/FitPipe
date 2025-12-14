package com.example.yourapp.ui.components // Or any other appropriate package

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.binus.fitpipe.ui.theme.Black90
import com.binus.fitpipe.ui.theme.Typo
import com.binus.fitpipe.ui.theme.White80

@Composable
fun FPScaffold(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Black90,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            // We pass through all the parameters from our custom scaffold.
            modifier = Modifier.fillMaxSize(),
            topBar = topBar,
            bottomBar = bottomBar,
            snackbarHost = snackbarHost,
            floatingActionButton = floatingActionButton,
            // THIS IS THE KEY: Make the Scaffold's content area transparent
            // so we can see the watermark text placed in the Box behind it.
            containerColor = backgroundColor,
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
            // We pass the content lambda directly to the real Scaffold.
            content = content,
        )
        Text(
            text = "Pose Tracker V1.0",
            style = Typo.MediumTwelve,
            color = White80,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter) // Pinned to the bottom-center
                    .padding(16.dp)
                    .systemBarsPadding(),
        )
    }
}
