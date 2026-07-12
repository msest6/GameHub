package com.example.gamehub.bela

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gamehub.data.BelaRepository

class BelaViewModelFactory(
    private val repository: BelaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BelaViewModel(repository) as T
    }
}