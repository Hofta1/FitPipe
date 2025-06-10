package com.binus.fitpipe.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FadingCircle(
    height: Int,
    width: Int,
) {
    val colorStops = arrayOf(
        0.0f to Yellow50,
        1.0f to Green20.copy(alpha = 0f)
    )
    val brush = Brush.radialGradient(
        colorStops = colorStops
    )
    Box(
        modifier = Modifier
            .size(height = height.dp, width = width.dp)
            .clip(CircleShape)
            .alpha(0.27f)
            .background(brush)
    )
}

@Preview
@Composable
private fun FadingCirclePreview() {
    FadingCircle(673, 581)
}