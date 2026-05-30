package com.example.schreibenaufdeutsch.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.example.schreibenaufdeutsch.data.remote.dto.GeneratedTaskDto
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination : NavKey {
    @Serializable
    data object Home : Destination
    
    @Serializable
    data object Favorites : Destination
    
    @Serializable
    data object Library : Destination
    
    @Serializable
    data class CreateTask(val initialTopic: String? = null) : Destination

    @Serializable
    data object Settings : Destination

    @Serializable
    data class PreviewTask(
        val generatedTask: GeneratedTaskDto? = null,
        val taskId: Long? = null,
        val level: String = "",
        val tone: String = "",
        val type: String = ""
    ) : Destination

    @Serializable
    data class PracticeTask(
        val taskId: Long,
        val variationIndex: Int = 0
    ) : Destination

    @Serializable
    data class TaskScore(
        val taskId: Long,
        val variationIndex: Int = 0,
        val sentencesJson: String
    ) : Destination
}
