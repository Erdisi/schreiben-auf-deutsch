package com.example.schreibenaufdeutsch.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schreibenaufdeutsch.SchreibenApp
import com.example.schreibenaufdeutsch.data.local.entity.WritingTask
import com.example.schreibenaufdeutsch.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _selectedLevel = MutableStateFlow("Alle")
    val selectedLevel: StateFlow<String> = _selectedLevel

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _allTasks = taskRepository.allTasks
    val totalTaskCount: StateFlow<Int> = _allTasks
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val tasks: StateFlow<List<WritingTask>> = combine(
        _allTasks,
        _selectedLevel,
        _searchQuery
    ) { allTasks, level, query ->
        allTasks.filter { 
            (level == "Alle" || it.germanLevel == level) && 
            (query.isBlank() || it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setLevelFilter(level: String) {
        _selectedLevel.value = level
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(task: WritingTask) {
        viewModelScope.launch {
            taskRepository.updateTask(task.copy(isFavorite = !task.isFavorite))
        }
    }
}
