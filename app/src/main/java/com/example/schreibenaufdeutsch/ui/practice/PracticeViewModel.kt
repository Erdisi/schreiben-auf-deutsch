package com.example.schreibenaufdeutsch.ui.practice

import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schreibenaufdeutsch.SchreibenApp
import com.example.schreibenaufdeutsch.data.local.PreferenceManager
import com.example.schreibenaufdeutsch.data.local.entity.Answer
import com.example.schreibenaufdeutsch.data.local.entity.WritingTask
import com.example.schreibenaufdeutsch.data.repository.AIRepository
import com.example.schreibenaufdeutsch.data.repository.TaskRepository
import com.example.schreibenaufdeutsch.util.LanguageUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Locale

@Serializable
data class PracticeSentence(
    val index: Int,
    val germanText: String,
    val translation: String,
    val isCompleted: Boolean = false,
    val isCurrent: Boolean = false,
    val hadError: Boolean = false,
    val aiFeedback: String? = null,
    val isAiLoading: Boolean = false,
    val lastInput: String = ""
)

class PracticeViewModel @JvmOverloads constructor(
    private val taskRepository: TaskRepository = SchreibenApp.instance.taskRepository,
    private val aiRepository: AIRepository = SchreibenApp.instance.aiRepository
) : ViewModel() {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _task = MutableStateFlow<WritingTask?>(null)
    val task: StateFlow<WritingTask?> = _task.asStateFlow()

    private val _sentences = MutableStateFlow<List<PracticeSentence>>(emptyList())
    val sentences: StateFlow<List<PracticeSentence>> = _sentences.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    private val _feedback = MutableStateFlow<String?>(null)
    val feedback: StateFlow<String?> = _feedback.asStateFlow()

    private val _submissionResult = MutableSharedFlow<Boolean>()
    val submissionResult: SharedFlow<Boolean> = _submissionResult.asSharedFlow()

    fun speak(text: String) {
        if (tts == null) {
            initTts {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        } else if (isTtsInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun initTts(onReady: () -> Unit = {}) {
        try {
            tts = TextToSpeech(SchreibenApp.instance) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.GERMANY
                    isTtsInitialized = true
                    onReady()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }

    private var lastLoadedVariationIndex: Int = -1

    fun loadTask(taskId: Long, variationIndex: Int = 0, initialSentences: List<PracticeSentence>? = null) {
        viewModelScope.launch {
            val loadedTask = taskRepository.getTaskById(taskId)
            _task.value = loadedTask
            _currentIndex.value = 0
            _isFinished.value = false
            _feedback.value = null
            lastLoadedVariationIndex = variationIndex
            
            if (initialSentences != null) {
                _sentences.value = initialSentences
                return@launch
            }

            loadedTask?.let { t ->
                val variation = t.variations.getOrNull(variationIndex) ?: t.variations.firstOrNull()
                
                if (variation != null) {
                    val germanSentences = LanguageUtils.splitIntoSentences(variation.text)
                    val translationSentences = LanguageUtils.splitIntoSentences(variation.translation)

                    val sentencesList = germanSentences.mapIndexed { index, german ->
                        PracticeSentence(
                            index = index,
                            germanText = german.trim(),
                            translation = translationSentences.getOrNull(index)?.trim() ?: ""
                        )
                    }
                    
                    _sentences.value = sentencesList.mapIndexed { index, sentence ->
                        sentence.copy(isCurrent = index == 0)
                    }
                }
            }
        }
    }

    fun submitSentence(input: String) {
        val currentIdx = _currentIndex.value
        val sentences = _sentences.value
        val target = sentences.getOrNull(currentIdx)?.germanText ?: ""
        
        val normalizedInput = normalizeText(input)
        val normalizedTarget = normalizeText(target)
        
        val isCorrect = normalizedInput == normalizedTarget
        
        _feedback.value = null
        
        val updatedSentences = sentences.map { 
            if (it.index == currentIdx) {
                it.copy(
                    isCompleted = true,
                    isCurrent = false,
                    lastInput = input,
                    hadError = !isCorrect
                )
            } else it
        }
        
        viewModelScope.launch {
            _submissionResult.emit(isCorrect)
        }

        val nextTarget = updatedSentences.find { !it.isCompleted && it.index > currentIdx }
            ?: updatedSentences.find { !it.isCompleted }
            ?: updatedSentences.find { it.hadError && it.index > currentIdx }
            ?: updatedSentences.find { it.hadError }

        if (nextTarget != null) {
            _sentences.value = updatedSentences.map { 
                it.copy(isCurrent = it.index == nextTarget.index)
            }
            _currentIndex.value = nextTarget.index
        } else {
            _sentences.value = updatedSentences
        }
    }

    fun completeTask() {
        finishTask()
    }

    fun selectSentence(index: Int) {
        val currentSentences = _sentences.value
        if (index !in currentSentences.indices) return

        _sentences.value = currentSentences.map {
            it.copy(isCurrent = it.index == index)
        }
        _currentIndex.value = index
        _feedback.value = null
    }

    fun clearFeedback() {
        _feedback.value = null
    }

    fun getAiFeedback(index: Int) {
        val sentence = _sentences.value.getOrNull(index) ?: return
        if (sentence.aiFeedback != null || sentence.isAiLoading) return

        viewModelScope.launch {
            _sentences.value = _sentences.value.map {
                if (it.index == index) it.copy(isAiLoading = true) else it
            }

            try {
                val languageCode = PreferenceManager.translationLanguageFlow.first()
                val targetLanguageName = LanguageUtils.getLanguageName(languageCode)

                val systemPrompt = """
                    You are a German teacher. Analyze the user's attempt at writing a German sentence.
                    Target sentence: ${sentence.germanText}
                    User's attempt: ${sentence.lastInput}
                    
                    Explain the mistakes (grammar, vocabulary, case, etc.) in a helpful way. 
                    Keep it short and encouraging. If the user was very close, mention that.
                    Focus on the most important errors.
                    
                    IMPORTANT: Provide the explanation in $targetLanguageName.
                """.trimIndent()

                val feedbackText = aiRepository.generateFeedback(systemPrompt)

                _sentences.value = _sentences.value.map {
                    if (it.index == index) it.copy(aiFeedback = feedbackText, isAiLoading = false) else it
                }
            } catch (e: Exception) {
                _sentences.value = _sentences.value.map {
                    if (it.index == index) it.copy(isAiLoading = false, aiFeedback = "Could not load feedback.") else it
                }
            }
        }
    }

    fun getSentencesJson(): String {
        return Json.encodeToString(_sentences.value)
    }

    private fun normalizeText(text: String): String {
        return text.trim()
            .replace(Regex("\\s+"), " ")
            .replace(Regex("[^a-zA-ZäöüßÄÖÜ0-9 ]"), "")
    }

    private fun finishTask() {
        _isFinished.value = true
        viewModelScope.launch {
            _task.value?.let { t ->
                val answer = Answer(
                    taskId = t.id,
                    userText = _sentences.value.joinToString("\n") { it.germanText },
                    score = 100,
                    feedback = "Great job! Task completed."
                )
                taskRepository.insertAnswer(answer)
                taskRepository.updateTask(t.copy(status = "Completed"))
            }
        }
    }
}
