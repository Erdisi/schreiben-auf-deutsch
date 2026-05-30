package com.example.schreibenaufdeutsch.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.schreibenaufdeutsch.data.local.converter.ListConverters
import com.example.schreibenaufdeutsch.data.local.dao.AnswerDao
import com.example.schreibenaufdeutsch.data.local.dao.WritingTaskDao
import com.example.schreibenaufdeutsch.data.local.entity.Answer
import com.example.schreibenaufdeutsch.data.local.entity.WritingTask

@Database(entities = [WritingTask::class, Answer::class], version = 2, exportSchema = false)
@TypeConverters(ListConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun writingTaskDao(): WritingTaskDao
    abstract fun answerDao(): AnswerDao

    companion object {
        const val DATABASE_NAME = "schreiben_auf_deutsch_db"
    }
}
