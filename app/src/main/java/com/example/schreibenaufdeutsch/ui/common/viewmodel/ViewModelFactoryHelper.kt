package com.example.schreibenaufdeutsch.ui.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
fun <T : ViewModel> viewModelFactory(initializer: () -> T): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return initializer() as T
        }
    }
}
