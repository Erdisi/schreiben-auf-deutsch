package com.example.schreibenaufdeutsch.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.schreibenaufdeutsch.data.local.entity.WritingTask
import kotlinx.coroutines.flow.Flow

@Dao
interface WritingTaskDao {
    @Query("SELECT * FROM writing_tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<WritingTask>>

    @Query("SELECT * FROM writing_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): WritingTask?

    @Query("SELECT * FROM writing_tasks WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteTasks(): Flow<List<WritingTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: WritingTask): Long

    @Update
    suspend fun updateTask(task: WritingTask)

    @Delete
    suspend fun deleteTask(task: WritingTask)
}
