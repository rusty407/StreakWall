package com.console.streakwall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.console.streakwall.ui.navigation.StreakWallApp
import com.console.streakwall.ui.theme.StreakWallTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StreakWallTheme {
                StreakWallApp(application = application as StreakWallApplication)
            }
        }
    }
}
