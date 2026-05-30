package com.example.schreibenaufdeutsch.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.schreibenaufdeutsch.data.local.entity.Answer
import kotlinx.coroutines.flow.Flow

@Dao
interface AnswerDao {
    @Query("SELECT * FROM answers WHERE taskId = :taskId ORDER BY createdAt DESC")
    fun getAnswersForTask(taskId: Long): Flow<List<Answer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: Answer): Long

    @Update
    suspend fun updateAnswer(answer: Answer)

    @Delete
    suspend fun deleteAnswer(answer: Answer)
}
