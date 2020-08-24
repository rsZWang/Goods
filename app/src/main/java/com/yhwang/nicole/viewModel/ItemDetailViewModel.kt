package com.yhwang.nicole.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yhwang.nicole.repository.ItemRepository

class ItemDetailViewModel(
    itemRepository: ItemRepository
) : ViewModel() {

    class ItemViewModelFactory(
        private val itemRepository: ItemRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ItemDetailViewModel(
                itemRepository
            ) as T
        }
    }
}