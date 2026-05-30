package com.example.schreibenaufdeutsch.data.repository

import com.example.schreibenaufdeutsch.data.local.dao.AnswerDao
import com.example.schreibenaufdeutsch.data.local.dao.WritingTaskDao
import com.example.schreibenaufdeutsch.data.local.entity.Answer
import com.example.schreibenaufdeutsch.data.local.entity.WritingTask
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val writingTaskDao: WritingTaskDao,
    private val answerDao: AnswerDao
) {
    val allTasks: Flow<List<WritingTask>> = writingTaskDao.getAllTasks()
    val favoriteTasks: Flow<List<WritingTask>> = writingTaskDao.getFavoriteTasks()

    suspend fun getTaskById(id: Long): WritingTask? = writingTaskDao.getTaskById(id)

    suspend fun insertTask(task: WritingTask): Long = writingTaskDao.insertTask(task)

    suspend fun updateTask(task: WritingTask) = writingTaskDao.updateTask(task)

    suspend fun deleteTask(task: WritingTask) = writingTaskDao.deleteTask(task)

    fun getAnswersForTask(taskId: Long): Flow<List<Answer>> = answerDao.getAnswersForTask(taskId)

    suspend fun insertAnswer(answer: Answer): Long = answerDao.insertAnswer(answer)
}
