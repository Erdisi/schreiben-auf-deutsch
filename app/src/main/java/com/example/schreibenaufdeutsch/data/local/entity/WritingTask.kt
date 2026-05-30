package com.example.schreibenaufdeutsch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "writing_tasks")
data class WritingTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val germanLevel: String,
    val tone: String,
    val taskType: String,
    val prompt: String,
    val variations: List<TaskVariation>,
    val imageUrl: String? = null,
    val isFavorite: Boolean = false,
    val status: String = "Not Started",
    val createdAt: Long = System.currentTimeMillis()
)
