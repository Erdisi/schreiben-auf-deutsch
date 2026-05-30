package com.example.schreibenaufdeutsch.ui.preview

import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schreibenaufdeutsch.SchreibenApp
import com.example.schreibenaufdeutsch.data.local.PreferenceManager
import com.example.schreibenaufdeutsch.data.local.entity.TaskVariation
import com.example.schreibenaufdeutsch.data.local.entity.WritingTask
import com.example.schreibenaufdeutsch.data.remote.dto.GeneratedTaskDto
import com.example.schreibenaufdeutsch.data.remote.dto.VariationDto
import com.example.schreibenaufdeutsch.data.repository.AIRepository
import com.example.schreibenaufdeutsch.data.repository.TaskRepository
import com.example.schreibenaufdeutsch.util.LanguageUtils
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.Locale

class PreviewViewModel(
    private val taskRepository: TaskRepository,
    private val aiRepository: AIRepository
) : ViewModel() {
    
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _existingTask = MutableStateFlow<WritingTask?>(null)
    val existingTask: StateFlow<WritingTask?> = _existingTask

    private var translator: Translator? = null
    private var currentTargetLanguage: String = TranslateLanguage.ENGLISH

    private val _translatedTitle = MutableStateFlow("")
    val translatedTitle: StateFlow<String> = _translatedTitle

    private val _translatedDescription = MutableStateFlow("")
    val translatedDescription: StateFlow<String> = _translatedDescription

    private val _refreshingIndexes = MutableStateFlow<Set<Int>>(emptySet())
    val refreshingIndexes: StateFlow<Set<Int>> = _refreshingIndexes

    private val _displayTask = MutableStateFlow<GeneratedTaskDto?>(null)
    val displayTaskState: StateFlow<GeneratedTaskDto?> = _displayTask

    init {
        viewModelScope.launch {
            PreferenceManager.translationLanguageFlow.collectLatest { language ->
                if (language != currentTargetLanguage || translator == null) {
                    currentTargetLanguage = language
                    setupTranslator(language)
                }
            }
        }

        tts = TextToSpeech(SchreibenApp.instance) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.GERMANY
                isTtsInitialized = true
            }
        }
    }

    private fun setupTranslator(targetLanguage: String) {
        translator?.close()
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.GERMAN)
            .setTargetLanguage(targetLanguage)
            .build()
        translator = Translation.getClient(options)
        
        val conditions = DownloadConditions.Builder().build()
        translator?.downloadModelIfNeeded(conditions)
        
        _existingTask.value?.let { translateTask(it.title, it.description) }
        _displayTask.value?.let { translateTask(it.title, it.description) }
    }

    fun speak(text: String) {
        if (isTtsInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun loadTask(taskId: Long) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId)
            _existingTask.value = task
            task?.let { translateTask(it.title, it.description) }
        }
    }

    fun translateTask(title: String, description: String) {
        viewModelScope.launch {
            translator?.translate(title)
                ?.addOnSuccessListener { _translatedTitle.value = it }
            translator?.translate(description)
                ?.addOnSuccessListener { _translatedDescription.value = it }
        }
    }

    override fun onCleared() {
        super.onCleared()
        translator?.close()
        tts?.stop()
        tts?.shutdown()
    }

    fun refreshSentence(task: GeneratedTaskDto, variationIndex: Int, sentenceIndex: Int, level: String, tone: String) {
        val variation = task.variations.getOrNull(variationIndex) ?: return
        val germanSentences = LanguageUtils.splitIntoSentences(variation.text)
        val targetSentence = germanSentences.getOrNull(sentenceIndex) ?: return

        viewModelScope.launch {
            _refreshingIndexes.value += sentenceIndex
            try {
                val languageCode = PreferenceManager.translationLanguageFlow.first()
                val targetLanguageName = LanguageUtils.getLanguageName(languageCode)

                val systemPrompt = """
                    You are a German teacher. The user wants an alternative version of a specific sentence in a writing exercise.
                    
                    Task Context: ${task.title} - ${task.description}
                    Level: $level
                    Tone: $tone
                    
                    Original Sentence: $targetSentence
                    
                    Provide ONE alternative sentence that is natural, grammatically correct, and fits the level and tone perfectly.
                    IMPORTANT: The output MUST be a SINGLE sentence.
                    
                    Return ONLY the JSON format:
                    {
                      "text": "Alternative German sentence",
                      "translation": "Accurate $targetLanguageName translation"
                    }
                """.trimIndent()

                val responseText = aiRepository.generateTask("", systemPrompt)
                
                val start = responseText.indexOf('{')
                val end = responseText.lastIndexOf('}')
                if (start != -1 && end != -1) {
                    val jsonStr = responseText.substring(start, end + 1)
                    val result = Json { ignoreUnknownKeys = true }.decodeFromString<VariationDto>(jsonStr)
                    
                    val newText = result.text
                    val newTranslation = result.translation

                    val newGermanList = germanSentences.toMutableList()
                    newGermanList[sentenceIndex] = newText
                    
                    val translationList = LanguageUtils.splitIntoSentences(variation.translation).toMutableList()
                    while (translationList.size < newGermanList.size) translationList.add("")
                    translationList[sentenceIndex] = newTranslation

                    val updatedVariation = variation.copy(
                        text = newGermanList.joinToString("\n"),
                        translation = translationList.joinToString("\n")
                    )

                    val updatedVariations = task.variations.toMutableList()
                    updatedVariations[variationIndex] = updatedVariation
                    
                    val updatedTask = task.copy(variations = updatedVariations)
                    _displayTask.value = updatedTask
                    saveExistingTaskChanges(updatedTask)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _refreshingIndexes.value -= sentenceIndex
            }
        }
    }

    fun updateSentence(task: GeneratedTaskDto, variationIndex: Int, sentenceIndex: Int, newGermanText: String) {
        val variation = task.variations.getOrNull(variationIndex) ?: return
        val germanSentences = LanguageUtils.splitIntoSentences(variation.text).toMutableList()
        
        if (sentenceIndex in germanSentences.indices) {
            germanSentences[sentenceIndex] = newGermanText
            
            val updatedVariation = variation.copy(
                text = germanSentences.joinToString("\n")
            )

            val updatedVariations = task.variations.toMutableList()
            updatedVariations[variationIndex] = updatedVariation
            
            val updatedTask = task.copy(variations = updatedVariations)
            _displayTask.value = updatedTask
            saveExistingTaskChanges(updatedTask)
        }
    }

    private fun saveExistingTaskChanges(updatedTask: GeneratedTaskDto) {
        val currentExisting = _existingTask.value ?: return
        viewModelScope.launch {
            val updatedWritingTask = currentExisting.copy(
                variations = updatedTask.variations.map { TaskVariation(it.text, it.translation) }
            )
            taskRepository.updateTask(updatedWritingTask)
            _existingTask.value = updatedWritingTask
        }
    }

    fun setDisplayTask(task: GeneratedTaskDto) {
        if (_displayTask.value == null) {
            _displayTask.value = task
        }
    }

    fun deleteTask(task: WritingTask) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }
}
