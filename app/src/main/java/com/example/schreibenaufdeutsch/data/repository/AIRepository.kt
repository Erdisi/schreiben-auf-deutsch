package com.example.schreibenaufdeutsch.data.repository

import com.example.schreibenaufdeutsch.data.remote.GroqApiService
import com.example.schreibenaufdeutsch.data.remote.GroqClient
import com.example.schreibenaufdeutsch.data.remote.GroqMessage
import com.example.schreibenaufdeutsch.data.remote.GroqRequest
import com.example.schreibenaufdeutsch.data.remote.GroqResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AIRepository(
    private val apiService: GroqApiService = GroqClient.service
) {
    suspend fun generateTopic(
        systemPrompt: String
    ): String? = withContext(Dispatchers.IO) {
        val request = GroqRequest(
            model = "llama-3.1-8b-instant",
            messages = listOf(GroqMessage("user", systemPrompt))
        )
        val response = apiService.generateTask(GroqClient.authHeader, request)
        response.choices.firstOrNull()?.message?.content?.trim()
    }

    suspend fun generateTask(
        systemPrompt: String,
        userPrompt: String
    ): String = withContext(Dispatchers.IO) {
        val request = GroqRequest(
            model = "llama-3.1-8b-instant",
            messages = listOf(
                GroqMessage("system", systemPrompt),
                GroqMessage("user", userPrompt)
            )
        )
        val response = apiService.generateTask(GroqClient.authHeader, request)
        response.choices.firstOrNull()?.message?.content?.trim() 
            ?: throw Exception("No response from AI")
    }

    suspend fun generateFeedback(
        systemPrompt: String
    ): String = withContext(Dispatchers.IO) {
        val request = GroqRequest(
            model = "llama-3.1-8b-instant",
            messages = listOf(GroqMessage("user", systemPrompt))
        )
        val response = apiService.generateTask(GroqClient.authHeader, request)
        response.choices.firstOrNull()?.message?.content?.trim()
            ?: throw Exception("No response from AI")
    }
}
