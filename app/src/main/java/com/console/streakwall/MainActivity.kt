package com.console.streakwall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.console.streakwall.ui.navigation.StreakWallApp
import com.console.streakwall.ui.theme.StreakWallTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StreakWallTheme {
                // Screens without a Scaffold (onboarding, the loading state) would otherwise sit on
                // the window background and ignore dark mode.
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    StreakWallApp(application = application as StreakWallApplication)
                }
            }
        }
    }
}
