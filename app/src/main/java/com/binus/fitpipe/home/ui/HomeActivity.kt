package com.binus.fitpipe.home.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.binus.fitpipe.home.nav.AppNavHost
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
                    modifier =
                        Modifier
                            .fillMaxSize(),
                ) { innerPadding ->
                    AppNavHost()
                }
            }
        }
    }

    private fun goToCameraActivity() {
    }
}
