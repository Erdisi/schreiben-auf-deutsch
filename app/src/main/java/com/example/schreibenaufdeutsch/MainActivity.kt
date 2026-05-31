package com.example.schreibenaufdeutsch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.example.schreibenaufdeutsch.data.local.PreferenceManager
import com.example.schreibenaufdeutsch.ui.navigation.MainNavHost
import com.example.schreibenaufdeutsch.ui.theme.SchreibenAufDeutschTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val themeMode by PreferenceManager.themeModeFlow.collectAsState(initial = "System")
            val darkTheme = when (themeMode) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            SchreibenAufDeutschTheme(darkTheme = darkTheme) {
                MainNavHost()
            }
        }
    }
}
