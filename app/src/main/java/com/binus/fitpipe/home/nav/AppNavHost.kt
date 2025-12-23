package com.binus.fitpipe.home.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.binus.fitpipe.home.data.FeedbackLogUI
import com.binus.fitpipe.home.nav.ItemKey.Item_Feedback_Log
import com.binus.fitpipe.home.nav.NavigationRoutes.CAMERA
import com.binus.fitpipe.home.nav.NavigationRoutes.FEEDBACK_LOG
import com.binus.fitpipe.home.nav.NavigationRoutes.HOME
import com.binus.fitpipe.home.ui.CameraScreen
import com.binus.fitpipe.home.ui.FeedbackLogScreen
import com.binus.fitpipe.home.ui.HomeScreen
import com.binus.fitpipe.home.ui.HomeViewModel

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val navAction =
        remember(navController) {
            AppNavAction(navController)
        }
    NavHost(navController, startDestination = "home") {
        composable(HOME) {
            HomeScreen(
                onItemClick = navAction::navigateToCamera,
            )
        }
        composable("$CAMERA/{exerciseTitle}") { backStackEntry ->
            val viewModel = hiltViewModel<HomeViewModel>()
            val uiState = viewModel.uiState.collectAsState()

            val exerciseTitle = backStackEntry.arguments?.getString("exerciseTitle")
            CameraScreen(
                exerciseTitle = exerciseTitle ?: "",
                onBackPressed = navAction::onBackPressed,
                uiState = uiState.value,
                onPoseDetected = {exerciseTitle, convertedLandmarkList ->
                    viewModel.saveLandmark(
                        exerciseTitle,
                        convertedLandmarkList
                    )
                },
                onOpeningFeedbackLog = {
                    navController.currentBackStackEntry?.savedStateHandle?.set(Item_Feedback_Log, viewModel.fullErrorMessages)
                    navAction.onOpeningFeedbackHistory()
                }
            )
        }
        composable(FEEDBACK_LOG) {
            val feedbackLog = navController.previousBackStackEntry?.savedStateHandle?.get<List<FeedbackLogUI>>(Item_Feedback_Log).orEmpty()
            FeedbackLogScreen(
                onBackPressed = navAction::onBackPressed,
                feedbackLog = feedbackLog
            )

        }
    }
}

object NavigationRoutes {
    const val HOME = "home"
    const val CAMERA = "camera"

    const val FEEDBACK_LOG = "feedback_log"

    fun cameraWithArgs(exerciseTitle: String): String {
        return "$CAMERA/$exerciseTitle"
    }

    fun feedbackLog(): String{
        return FEEDBACK_LOG
    }

}

object ItemKey{
    const val Item_Feedback_Log = "Item_Feedback_Log"
}
