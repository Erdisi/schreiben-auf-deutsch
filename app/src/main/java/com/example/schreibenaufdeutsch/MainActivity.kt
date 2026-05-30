package com.example.schreibenaufdeutsch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.example.schreibenaufdeutsch.ui.navigation.MainNavHost
import com.example.schreibenaufdeutsch.ui.theme.SchreibenAufDeutschTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            SchreibenAufDeutschTheme {
                MainNavHost()
            }
        }
    }
}
