package com.example.schreibenaufdeutsch.data.remote

import com.example.schreibenaufdeutsch.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.io.IOException

@Serializable
data class GroqRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val response_format: GroqResponseFormat? = null
)

@Serializable
data class GroqMessage(
    val role: String,
    val content: String
)

@Serializable
data class GroqResponseFormat(
    val type: String = "json_object"
)

@Serializable
data class GroqResponse(
    val choices: List<GroqChoice>
)

@Serializable
data class GroqChoice(
    val message: GroqMessage
)

interface GroqApiService {
    @POST("chat/completions")
    suspend fun generateTask(
        @Header("Authorization") token: String,
        @Body request: GroqRequest
    ): GroqResponse
}

object GroqClient {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val retryInterceptor = Interceptor { chain ->
        var request = chain.request()
        var response: Response? = null
        var responseCode = -1
        var tryCount = 0
        val maxLimit = 3

        while (tryCount < maxLimit) {
            try {
                response = chain.proceed(request)
                responseCode = response.code
                if (response.isSuccessful) return@Interceptor response
                
                if (responseCode == 429) {
                    val waitTime = (tryCount + 1) * 1000L // Reduced from 2000L
                    Thread.sleep(waitTime)
                } else if (responseCode >= 500) {
                    Thread.sleep(500L) // Reduced from 1000L
                } else {
                    return@Interceptor response
                }
            } catch (e: IOException) {
                if (tryCount >= maxLimit - 1) throw e
            } finally {
                if (responseCode != -1 && responseCode != 200) {
                    response?.close()
                }
            }
            tryCount++
        }
        response ?: throw IOException("Failed to connect to Groq API")
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(retryInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.groq.com/openai/v1/")
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .client(client)
        .build()

    val service: GroqApiService = retrofit.create(GroqApiService::class.java)
    
    val authHeader: String 
        get() = "Bearer ${BuildConfig.GROQ_API_KEY}"
}
