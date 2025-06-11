package com.binus.fitpipe.home.nav

import androidx.navigation.NavHostController

class AppNavAction (private val navController: NavHostController) {
    fun navigateToCamera(exerciseTitle: String) {
        navController.navigate(NavigationRoutes.cameraWithArgs(exerciseTitle))
    }
    fun onBackPressed() {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        }
    }
}