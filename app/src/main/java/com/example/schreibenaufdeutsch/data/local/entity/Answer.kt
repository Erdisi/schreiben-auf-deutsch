package com.example.schreibenaufdeutsch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "answers")
data class Answer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val userText: String,
    val feedback: String? = null,
    val score: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)
