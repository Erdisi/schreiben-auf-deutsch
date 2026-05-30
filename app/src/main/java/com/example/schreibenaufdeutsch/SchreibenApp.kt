package com.example.schreibenaufdeutsch

import android.app.Application
import androidx.room.Room
import com.example.schreibenaufdeutsch.data.local.AppDatabase
import com.example.schreibenaufdeutsch.data.repository.AIRepository
import com.example.schreibenaufdeutsch.data.repository.TaskRepository

class SchreibenApp : Application() {

    lateinit var database: AppDatabase
        private set

    val taskRepository: TaskRepository by lazy {
        TaskRepository(database.writingTaskDao(), database.answerDao())
    }
    
    val aiRepository: AIRepository by lazy {
        AIRepository()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    companion object {
        lateinit var instance: SchreibenApp
            private set
    }
}
