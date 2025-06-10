package com.example.yourapp.ui.components // Or any other appropriate package

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A custom Scaffold wrapper that adds a default background text watermark.
 *
 * @param modifier The modifier to be applied to the scaffold.
 * @param backgroundText The text to be displayed as a watermark at the bottom. If null, no text is shown.
 * @param topBar The top app bar of the screen.
 * @param bottomBar The bottom bar of the screen.
 * @param snackbarHost The snackbar host of the screen.
 * @param floatingActionButton The floating action button of the screen.
 * @param content The main content of the screen. The background of this content area is
 * transparent to allow the watermark to show through. You should apply your own
 * background to the content's root composable (e.g., a Column or LazyColumn).
 */
@Composable
fun CustomScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Pose Tracker V1.0",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) // Subtle color
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter) // Pinned to the bottom-center
                .padding(8.dp)
        )

        // 2. The Actual Scaffold
        Scaffold(
            // We pass through all the parameters from our custom scaffold.
            modifier = Modifier.fillMaxSize(),
            topBar = topBar,
            bottomBar = bottomBar,
            snackbarHost = snackbarHost,
            floatingActionButton = floatingActionButton,
            // THIS IS THE KEY: Make the Scaffold's content area transparent
            // so we can see the watermark text placed in the Box behind it.
            containerColor = Color.Transparent,
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
            // We pass the content lambda directly to the real Scaffold.
            content = content
        )
    }
}