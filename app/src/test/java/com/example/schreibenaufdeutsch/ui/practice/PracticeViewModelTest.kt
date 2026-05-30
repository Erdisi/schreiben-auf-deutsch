package com.example.schreibenaufdeutsch.ui.practice

import com.example.schreibenaufdeutsch.SchreibenApp
import com.example.schreibenaufdeutsch.data.local.dao.AnswerDao
import com.example.schreibenaufdeutsch.data.local.dao.WritingTaskDao
import com.example.schreibenaufdeutsch.data.local.entity.WritingTask
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PracticeViewModelTest {

    private val taskDao = mockk<WritingTaskDao>(relaxed = true)
    private val answerDao = mockk<AnswerDao>(relaxed = true)
    private lateinit var viewModel: PracticeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock SchreibenApp.instance
        mockkObject(SchreibenApp.Companion)
        val mockApp = mockk<SchreibenApp>(relaxed = true)
        every { SchreibenApp.instance } returns mockApp
        
        viewModel = PracticeViewModel(taskDao, answerDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `loadTask correctly parses sentences from variation`() = runTest {
        val taskId = 1L
        val task = WritingTask(
            id = taskId,
            title = "Test Task",
            description = "Test Description",
            germanLevel = "A1",
            tone = "Informal",
            taskType = "Daily",
            prompt = "Test Prompt",
            variations = listOf("Satz 1\nSatz 2|Sentence 1\nSentence 2")
        )
        
        coEvery { taskDao.getTaskById(taskId) } returns task
        
        viewModel.loadTask(taskId)
        advanceUntilIdle()
        
        val sentences = viewModel.sentences.value
        assertEquals(2, sentences.size)
        assertEquals("Satz 1", sentences[0].germanText)
        assertEquals("Sentence 1", sentences[0].translation)
        assertTrue(sentences[0].isCurrent)
        assertEquals("Satz 2", sentences[1].germanText)
        assertEquals("Sentence 2", sentences[1].translation)
        assertFalse(sentences[1].isCurrent)
    }

    @Test
    fun `submitSentence updates current index and completes sentence`() = runTest {
        val taskId = 1L
        val task = WritingTask(
            id = taskId,
            title = "Test Task",
            description = "Test Description",
            germanLevel = "A1",
            tone = "Informal",
            taskType = "Daily",
            prompt = "Test Prompt",
            variations = listOf("Satz 1\nSatz 2|Sentence 1\nSentence 2")
        )
        coEvery { taskDao.getTaskById(taskId) } returns task
        
        viewModel.loadTask(taskId)
        advanceUntilIdle()
        
        // Submit first sentence correctly
        viewModel.submitSentence("Satz 1")
        
        val sentencesAfterFirst = viewModel.sentences.value
        assertTrue(sentencesAfterFirst[0].isCompleted)
        assertFalse(sentencesAfterFirst[0].hadError)
        assertFalse(sentencesAfterFirst[0].isCurrent)
        assertTrue(sentencesAfterFirst[1].isCurrent)
        assertEquals(1, viewModel.currentIndex.value)
        
        // Submit second sentence with error
        viewModel.submitSentence("Wrong text")
        
        val sentencesAfterSecond = viewModel.sentences.value
        assertTrue(sentencesAfterSecond[1].isCompleted)
        assertTrue(sentencesAfterSecond[1].hadError)
        assertTrue(viewModel.isFinished.value)
    }

    @Test
    fun `submitSentence normalization handles case and punctuation`() = runTest {
        val taskId = 1L
        val task = WritingTask(
            id = taskId,
            title = "Test Task",
            description = "Test Description",
            germanLevel = "A1",
            tone = "Informal",
            taskType = "Daily",
            prompt = "Test Prompt",
            variations = listOf("Hallo, wie geht's?|Hello, how are you?")
        )
        coEvery { taskDao.getTaskById(taskId) } returns task
        
        viewModel.loadTask(taskId)
        advanceUntilIdle()
        
        // Submit with different case and extra spaces/punctuation
        viewModel.submitSentence("  hallo wie gehts  ")
        
        val sentences = viewModel.sentences.value
        assertTrue(sentences[0].isCompleted)
        assertFalse(sentences[0].hadError)
    }

    @Test
    fun `finishTask saves answer and updates task status`() = runTest {
        val taskId = 1L
        val task = WritingTask(
            id = taskId,
            title = "Test Task",
            description = "Test Description",
            germanLevel = "A1",
            tone = "Informal",
            taskType = "Daily",
            prompt = "Test Prompt",
            variations = listOf("Satz 1|Sentence 1")
        )
        coEvery { taskDao.getTaskById(taskId) } returns task
        
        viewModel.loadTask(taskId)
        advanceUntilIdle()
        
        viewModel.submitSentence("Satz 1")
        advanceUntilIdle()
        
        assertTrue(viewModel.isFinished.value)
        coVerify { answerDao.insertAnswer(any()) }
        coVerify { taskDao.updateTask(match { it.status == "Completed" }) }
    }
}
