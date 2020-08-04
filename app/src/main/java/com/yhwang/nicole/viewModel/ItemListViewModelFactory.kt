package com.yhwang.nicole.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yhwang.nicole.repository.ItemListRepository

class ItemListViewModelFactory(
    private val itemListRepository: ItemListRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ItemListViewModel(
            itemListRepository
        ) as T
    }
}