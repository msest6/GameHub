package com.example.gamehub.uno

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gamehub.data.UnoRepository

class UnoViewModelFactory(
    private val repository: UnoRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UnoViewModel(repository) as T
    }
}