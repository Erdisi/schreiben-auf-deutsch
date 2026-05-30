package com.example.schreibenaufdeutsch.data.local.entity

import kotlinx.serialization.Serializable

@Serializable
data class TaskVariation(
    val text: String,
    val translation: String
)
