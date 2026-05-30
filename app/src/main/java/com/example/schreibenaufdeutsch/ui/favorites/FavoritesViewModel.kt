package com.example.schreibenaufdeutsch.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schreibenaufdeutsch.SchreibenApp
import com.example.schreibenaufdeutsch.data.local.entity.WritingTask
import com.example.schreibenaufdeutsch.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _selectedLevel = MutableStateFlow("Alle")
    val selectedLevel: StateFlow<String> = _selectedLevel

    val favoriteTasks: StateFlow<List<WritingTask>> = combine(
        taskRepository.favoriteTasks,
        _selectedLevel
    ) { allFavorites, level ->
        if (level == "Alle") allFavorites else allFavorites.filter { it.germanLevel == level }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setLevelFilter(level: String) {
        _selectedLevel.value = level
    }

    fun toggleFavorite(task: WritingTask) {
        viewModelScope.launch {
            taskRepository.updateTask(task.copy(isFavorite = !task.isFavorite))
        }
    }
}
