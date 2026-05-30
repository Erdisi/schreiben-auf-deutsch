package com.example.schreibenaufdeutsch.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeneratedTaskDto(
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("image_query")
    val image_query: String = "",
    @SerialName("variations")
    val variations: List<VariationDto>,
)

@Serializable
data class VariationDto(
    @SerialName("text")
    val text: String,
    @SerialName("translation")
    val translation: String,
)
