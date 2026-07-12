package com.example.gamehub.darts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gamehub.data.DartsRepository
import com.example.gamehub.data.model.CheckoutTable

class DartsViewModelFactory(
    private val repository: DartsRepository,
    private val checkoutTable: List<CheckoutTable>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DartsViewModel(repository, checkoutTable) as T
    }
}