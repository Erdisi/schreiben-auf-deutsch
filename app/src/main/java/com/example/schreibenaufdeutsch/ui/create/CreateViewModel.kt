package com.example.schreibenaufdeutsch.ui.create

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schreibenaufdeutsch.SchreibenApp
import com.example.schreibenaufdeutsch.data.local.PreferenceManager
import com.example.schreibenaufdeutsch.data.local.entity.TaskVariation
import com.example.schreibenaufdeutsch.data.local.entity.WritingTask
import com.example.schreibenaufdeutsch.data.remote.dto.GeneratedTaskDto
import com.example.schreibenaufdeutsch.data.repository.AIRepository
import com.example.schreibenaufdeutsch.data.repository.TaskRepository
import com.example.schreibenaufdeutsch.util.LanguageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.random.Random

class CreateViewModel(
    private val taskRepository: TaskRepository,
    private val aiRepository: AIRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<CreateUiState>(CreateUiState.Idle)
    val uiState: StateFlow<CreateUiState> = _uiState
    
    private val _exampleTopic = MutableStateFlow<String?>(null)
    val exampleTopic: StateFlow<String?> = _exampleTopic

    private val _isGeneratingExample = MutableStateFlow(false)
    val isGeneratingExample: StateFlow<Boolean> = _isGeneratingExample

    companion object {
        private val jsonParser = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
    }

    fun generateExampleTopic(
        level: String,
        tone: String,
        type: String,
        track: String
    ) {
        if (!isNetworkAvailable()) {
            _uiState.value = CreateUiState.Error("No internet connection.")
            return
        }

        viewModelScope.launch {
            _isGeneratingExample.value = true
            try {
                val systemPrompt = """
                    You are an expert German teacher. 
                    Generate a short, realistic, and engaging German writing task topic (max 10 words).
                    The topic must be suitable for:
                    Track: $track
                    Level: $level
                    Tone: $tone
                    Type: $type
                    
                    Return ONLY the topic in German. No quotes, no explanations.
                """.trimIndent()

                val topic = aiRepository.generateTopic(systemPrompt)
                    ?: throw Exception("No response from AI")
                
                _exampleTopic.value = topic
            } catch (e: Exception) {
                _uiState.value = CreateUiState.Error("Failed to generate example: ${e.message}")
            } finally {
                _isGeneratingExample.value = false
            }
        }
    }

    fun clearExampleTopic() {
        _exampleTopic.value = null
    }

    fun generateTask(
        description: String,
        level: String,
        tone: String,
        type: String,
        extraContext: String,
        complexity: Float,
        track: String = "General",
        grammarFocus: List<String> = emptyList(),
        mustUseWords: String = "",
        isFavorite: Boolean = false
    ) {
        if (!isNetworkAvailable()) {
            _uiState.value = CreateUiState.Error("No internet connection. Please check your network.")
            return
        }

        viewModelScope.launch {
            if (!PreferenceManager.canGenerateTask()) {
                _uiState.value = CreateUiState.Error("Tägliches Limit erreicht (5 Aufgaben). Bitte versuchen Sie es morgen wieder!")
                return@launch
            }

            _uiState.value = CreateUiState.Loading
            try {
                val difficulty = when {
                    complexity < 0.33f -> "Easy"
                    complexity > 0.66f -> "Advanced"
                    else -> "Normal"
                }

                val languageCode = PreferenceManager.translationLanguageFlow.first()
                val targetLanguageName = LanguageUtils.getLanguageName(languageCode)

                val trackInstructions = when (track) {
                    "DTB (Beruf)" -> """
                        You are an expert DTB (Deutsch-Test für den Beruf) examiner. 
                        Focus strictly on workplace communication, internal emails, and professional business scenarios. 
                        LENGTH RULE: You MUST generate at least 150-180 words.
                        STRUCTURE: Use a formal business greeting, a clear professional reason for writing, detailed supporting points, and a standard professional closing.
                    """.trimIndent()
                    "Telc" -> """
                        You are an expert Telc examiner. 
                        Focus on formal complaints, inquiries, and requests for information.
                        LENGTH RULE: You MUST generate at least 150-200 words.
                        STRUCTURE: Follow the classic Telc B2 structure: Salutation, Reference to the situation, detailed explanation of 3-4 specific points, and a formal closing/request for action.
                    """.trimIndent()
                    "Goethe" -> """
                        You are an expert Goethe-Institut examiner. 
                        Focus on social forum posts, detailed opinions, and formal messages.
                        LENGTH RULE: You MUST generate at least 180 words for B2 and 150 words for B1.
                        STRUCTURE: For forum posts, ensure a clear introduction of the topic, detailed expression of opinion with examples, and a well-reasoned alternative proposal.
                    """.trimIndent()
                    else -> """
                        You are an expert German teacher. 
                        LENGTH RULE: Generate a substantial writing exercise of at least 120-150 words.
                        Ensure the variations provide enough volume for high-quality practice.
                    """.trimIndent()
                }

                val grammarConstraint = if (grammarFocus.isNotEmpty()) {
                    "CORE PEDAGOGICAL RULE: You MUST heavily prioritize using the following German grammar structures in the variations: ${grammarFocus.joinToString(", ")}."
                } else ""

                val vocabularyConstraint = if (mustUseWords.isNotBlank()) {
                    "VOCABULARY RULE: You MUST naturally incorporate these specific words or phrases into the German text: $mustUseWords."
                } else ""

                val systemPrompt = """
                    $trackInstructions
                    $grammarConstraint
                    $vocabularyConstraint
                    
                    Your task is to generate a German writing exercise based on user input parameters.
                    You MUST return the response as a valid JSON object.
                    Do NOT include markdown, explanations, or any text outside the JSON.

                    OUTPUT FORMAT:
                    {
                      "title": "Title in German",
                      "description": "Short task description in German only",
                      "image_query": "A clear, descriptive English prompt for an image generator",
                      "variations": [
                        {
                          "text": "German text with EACH sentence on a NEW LINE (\n)",
                          "translation": "Accurate $targetLanguageName translation"
                        }
                      ]
                    }

                    CORE RULES:
                    1. Each sentence in the "text" field MUST be on its own line using \n. Do NOT combine multiple sentences into a single line.
                    2. Difficulty & Volume Rules: 
                       - Easy: One level lower than requested, simple grammar, ~100 words.
                       - Normal: Exact level requested, standard exam length (150-180 words).
                       - Advanced: Slightly above level, complex connectors (obwohl, darüber hinaus), ~200+ words.
                    3. TASK DEPTH: Generate exactly ONE high-quality, comprehensive variation. 
                    4. VARIATION DENSITY: The text MUST consist of 10-18 sentences to provide thorough practice.
                    5. IMAGE RULE: The "image_query" MUST be in English. Focus strictly on professional objects, architecture, or clean cinematic environments relevant to the task. 
                       - CRITICAL: Do NOT include humans, faces, or body parts. 
                       - STYLE: Use 'Professional product photography' or 'Minimalist still life'.
                    6. Incorporate the provided "Extra Context" into the scenario and arguments of the variations.
                """.trimIndent()

                val userPrompt = """
                    Topic: $description
                    Level: $level
                    Tone: $tone
                    Category: $type
                    Track: $track
                    Difficulty: $difficulty
                    Extra Context: $extraContext
                    Grammar Focus: ${grammarFocus.joinToString(", ")}
                    Specific Vocabulary: $mustUseWords
                """.trimIndent()

                val responseText = aiRepository.generateTask(systemPrompt, userPrompt)
                val jsonString = extractJson(responseText)

                try {
                    val generatedTask = jsonParser.decodeFromString<GeneratedTaskDto>(jsonString)
                    
                    val encodedQuery = URLEncoder.encode(generatedTask.image_query, StandardCharsets.UTF_8.toString())
                    val randomSeed = Random.nextInt(1000, 99999)
                    val pollinationsUrl = "https://image.pollinations.ai/prompt/$encodedQuery?nologo=true&seed=$randomSeed&model=flux"

                    val writingTask = WritingTask(
                        title = generatedTask.title,
                        description = description,
                        germanLevel = level,
                        tone = tone,
                        taskType = type,
                        prompt = generatedTask.description,
                        variations = generatedTask.variations.map { 
                            TaskVariation(it.text, it.translation)
                        },
                        imageUrl = if (generatedTask.image_query.isNotBlank()) pollinationsUrl else null,
                        isFavorite = isFavorite
                    )
                    val id = taskRepository.insertTask(writingTask)
                    PreferenceManager.incrementDailyTaskCount()
                    _uiState.value = CreateUiState.Success(id)
                } catch (e: Exception) {
                    android.util.Log.e("GroqParse", "Error: ${e.message}\nJSON: $jsonString")
                    throw Exception("The AI returned an invalid format. Please try one more time.")
                }
            } catch (e: Exception) {
                android.util.Log.e("CreateViewModel", "Error generating task", e)
                val errorMessage = when {
                    e is kotlinx.serialization.SerializationException -> "The AI sent invalid data. Please try again."
                    e is java.io.IOException -> "Network error. Please try again later."
                    e.message?.contains("429") == true -> "Server is busy (Too many requests). Waiting 10 seconds might help."
                    else -> e.message ?: "An unexpected error occurred"
                }
                _uiState.value = CreateUiState.Error(errorMessage)
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = SchreibenApp.instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    fun resetState() {
        _uiState.value = CreateUiState.Idle
    }

    private fun extractJson(text: String): String {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        return if (start != -1 && end != -1 && end > start) {
            text.substring(start, end + 1)
        } else {
            text
        }
    }
}

sealed interface CreateUiState {
    data object Idle : CreateUiState
    data object Loading : CreateUiState
    data class Success(val taskId: Long) : CreateUiState
    data class Error(val message: String) : CreateUiState
}
