package com.example.schreibenaufdeutsch.util

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette

object DynamicThemeManager {
    var currentSeedColor by mutableStateOf<Color?>(null)
        private set

    fun updateSeedColorFromBitmap(bitmap: Bitmap?) {
        if (bitmap == null) return
        
        Palette.from(bitmap).generate { palette ->
            // Prioritize vibrant and readable colors
            val selectedSwatch = palette?.vibrantSwatch 
                ?: palette?.lightVibrantSwatch 
                ?: palette?.mutedSwatch
                ?: palette?.dominantSwatch
            
            selectedSwatch?.let { swatch ->
                currentSeedColor = Color(swatch.rgb)
            }
        }
    }

    fun reset() {
        currentSeedColor = null
    }
}
