package com.binus.fitpipe.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.binus.fitpipe.R
import com.binus.fitpipe.ui.theme.Black70
import com.binus.fitpipe.ui.theme.FitPipeTheme
import com.binus.fitpipe.ui.theme.Grey70
import com.binus.fitpipe.ui.theme.Red50
import com.binus.fitpipe.ui.theme.Typo
import com.binus.fitpipe.ui.theme.White80
import com.binus.fitpipe.ui.theme.Yellow50
import org.w3c.dom.Text

@Composable
internal fun CameraScreen(
    exerciseTitle: String,
    onBackPressed: () -> Unit,
) {
    FitPipeTheme {
        CameraScreen(
            exerciseTitle = exerciseTitle,
            modifier = Modifier,
            onBackPressed = onBackPressed
        )
    }
}

@Composable
private fun CameraScreen(
    exerciseTitle: String,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
) {
    Column {
        Spacer(Modifier.size(75.dp))
        Box(
            modifier = modifier
                .fillMaxWidth()
        ) {
            BackButton { onBackPressed() }
            Text(
                text = exerciseTitle,
                style = Typo.BoldTwentyFour,
                color = White80,
                modifier = modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        //For Camera Preview
        Spacer(modifier.size(551.dp))

        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(Black70)
                .padding(vertical = 26.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Count: 9",
                style = Typo.MediumEighteen,
                color = White80,
                textAlign = TextAlign.Center,
                modifier = modifier.fillMaxWidth()
            )
            Spacer(modifier.size(14.dp))
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = modifier
                    .height(36.dp)
            ){
                Text(
                    text = "Feedback Text Long Text Te",
                    style = Typo.BoldTwenty,
                    color = if(true) Yellow50 else Red50,// Replace with actual condition for feedback color
                    textAlign = TextAlign.Center,
                    modifier = modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BackButton(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
) {
    Box(modifier = modifier
        .clip(CircleShape)
        .background(Grey70)
        .size(30.dp)
        .clickable { onBackPressed() },
        contentAlignment = Alignment.Center
    ){
        Image(
            painter = painterResource(R.drawable.line),
            contentDescription = "Back Button",
            modifier = modifier
                .padding(8.dp)
                .fillMaxSize()
        )
    }
}

@Preview
@Composable
private fun BackButtonPreview() {
    BackButton(
        modifier = Modifier,
        onBackPressed = {}
    )
}

@Preview
@Composable
private fun CameraScreenPreview() {
    CameraScreen(
        exerciseTitle = "Push Up",
        onBackPressed = {}
    )
}


