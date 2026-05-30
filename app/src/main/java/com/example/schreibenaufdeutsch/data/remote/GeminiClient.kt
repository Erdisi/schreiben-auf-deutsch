package com.example.schreibenaufdeutsch.data.remote

import com.example.schreibenaufdeutsch.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel

object GeminiClient {
    val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.API_KEY
    )
}
