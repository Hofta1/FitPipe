package com.binus.fitpipe.home.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.binus.fitpipe.onboarding.ui.OnboardingScreen
import com.binus.fitpipe.ui.theme.Black80
import com.binus.fitpipe.ui.theme.Black90
import com.binus.fitpipe.ui.theme.FitPipeTheme
import com.example.yourapp.ui.components.FPScaffold
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitPipeTheme {
                FPScaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Black80)
                ) { innerPadding ->
//                    OnboardingScreen(
//                        onStart = {goToHomeScreen()}
//                    )
                }
            }
        }
    }

    private fun goToHomeScreen() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }
}