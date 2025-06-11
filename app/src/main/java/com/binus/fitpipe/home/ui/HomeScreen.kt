package com.binus.fitpipe.home.ui

import android.inputmethodservice.Keyboard.Row
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.binus.fitpipe.R
import com.binus.fitpipe.ui.theme.Black70
import com.binus.fitpipe.ui.theme.FitPipeTheme
import com.binus.fitpipe.ui.theme.Typo
import com.binus.fitpipe.ui.theme.White80
import com.example.yourapp.ui.components.FPScaffold

@Composable
internal fun HomeScreen(
    onItemClick: (String) -> Unit
){
    FitPipeTheme {
        HomeScreen(
            modifier = Modifier,
            onItemClick = onItemClick
        )
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    onItemClick: (String) -> Unit
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val state = viewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()
    Box(
        modifier = modifier.padding(horizontal = 30.dp),
    ) {
        Column(){
            Spacer(Modifier.size(75.dp))
            Text(
                text = "Hello,",
                style = Typo.ExtraBoldTwentyFour,
                color = White80
            )
            Spacer(Modifier.size(14.dp))
            Text(
                text = "Welcome to Pose Tracker",
                style = Typo.MediumSixteen,
                color = White80
            )
            Spacer(Modifier.size(48.dp))
            LazyColumn(
                state = lazyListState
            ) {
                itemsIndexed(
                    state.value.rows,
                    key = { _, item -> item.title } // Use the string as the key for each item
                ) { index, item ->
                    ExerciseRow(
                        title = item.title,
                        imageResourceId = item.imageResourceId,
                        onItemClick = onItemClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseRow(
    modifier: Modifier = Modifier,
    title: String,
    imageResourceId: Int,
    onItemClick: (String) -> Unit
) {
    Row(
        modifier = modifier
            .padding(bottom = 22.dp)
            .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp, topStart = 16.dp, bottomStart = 16.dp))
            .fillMaxWidth()
            .height(78.dp)
            .background(color = Black70)
            .clickable { onItemClick(title) },
    ){
        Text(
            text = title,
            style = Typo.BoldTwentyFour,
            color = White80,
            modifier = Modifier
                .padding(top = 24.dp, start = 28.dp)
                .weight(1f)
        )
        Image(
            painter = painterResource(id = imageResourceId),
            contentDescription = null,
            modifier = Modifier
                .padding(end = 28.dp)
                .fillMaxHeight()
        )
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    FPScaffold {
        HomeScreen(
            onItemClick = {}
        )
    }
}

@Preview
@Composable
private fun ExerciseRowPreview() {
    FitPipeTheme {
        ExerciseRow(
            title = "Push Up",
            imageResourceId = R.drawable.jumping_jack_icon, // Replace with a valid image resource ID
            onItemClick = {}
        )
    }
}