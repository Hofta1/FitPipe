package com.binus.fitpipe.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.binus.fitpipe.home.data.FeedbackLogUI
import com.binus.fitpipe.ui.theme.Black70
import com.binus.fitpipe.ui.theme.FitPipeTheme
import com.binus.fitpipe.ui.theme.Typo
import com.binus.fitpipe.ui.theme.White80
import com.example.yourapp.ui.components.FPScaffold

@Composable
internal fun FeedbackLogScreen(
    onBackPressed: () -> Unit,
    feedbackLog: List<FeedbackLogUI>,
    ) {
    FitPipeTheme {
        FeedbackLogScreen(
            modifier = Modifier,
            onBackPressed = onBackPressed,
            feedbackLog = feedbackLog
        )
    }
}

@Composable
private fun FeedbackLogScreen(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
    feedbackLog: List<FeedbackLogUI>
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        Box(Modifier
            .fillMaxWidth(),
            ){
            BackButton{onBackPressed()}
            Text(
                text = "Feedback Log",
                textAlign = TextAlign.Center,
                style = Typo.BoldTwentyFour,
                color = White80,
                modifier = modifier.align(Alignment.Center)
            )
        }
        Spacer(Modifier.size(16.dp))
        if(feedbackLog.isNotEmpty()){
            LazyColumn {
                items(feedbackLog.size) { index ->
                    FeedbackLogRow(
                        feedbackLog = feedbackLog[index].fullFeedback,
                        feedbackStatus = feedbackLog[index].status
                    )
                }
            }
        }
        else{
            EmptyScreen()
        }
    }
}

@Composable
private fun EmptyScreen(modifier: Modifier = Modifier){
    Box(
        modifier = modifier.fillMaxSize(),
    ){
        Box(
            modifier = modifier.matchParentSize()
                .alpha(0.5f)
        )
        Text(
            text = "There is no feedback logged",
            style = Typo.BoldTwentyFour,
            color = White80,
            modifier = modifier
                .align(Alignment.Center)
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FeedbackLogRow(
    modifier: Modifier = Modifier,
    feedbackLog: String,
    feedbackStatus: Boolean,
) {
    Row(
        modifier =
            modifier
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp, topStart = 16.dp, bottomStart = 16.dp))
                .fillMaxWidth()
                .height(78.dp)
                .background(color = Black70),
    ) {
        Text(
            text = feedbackLog,
            style = Typo.MediumEighteen,
            color = White80,
            modifier =
                Modifier
                    .padding(top = 24.dp, start = 28.dp)
                    .weight(1f),
            maxLines = 2
        )
        Box(
            modifier = modifier
                .padding(top = 24.dp, end = 28.dp)
                .size(24.dp)
                .background(
                    if (feedbackStatus) Color.Green else Color.Red,
                    shape = CircleShape
                )
        )
    }
}

@Preview
@Composable
private fun FeedbackLogScreenPreview() {
    FPScaffold {
        FeedbackLogScreen(
            onBackPressed = {},
            feedbackLog = listOf(
                FeedbackLogUI(
                    "Goo awdawdawdawdawdawdaw dawdwadawdwa awdawdawdawd awdawdaw dwaawd",
                    true
                ),
                FeedbackLogUI(
                    "Bad",
                    false
                ),
            )
        )
    }
}

@Preview
@Composable
private fun FeedbackLogRowPreview() {
    FitPipeTheme {
        FeedbackLogRow(
            feedbackLog = "This is feedback hahahaha haahah ah ha hah wa ah hhdawhdahdw",
            feedbackStatus = true
        )
    }
}
