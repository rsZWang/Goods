package com.yhwang.nicole.viewModel

import androidx.lifecycle.ViewModel
import com.yhwang.nicole.repository.ItemListRepository

class ItemListViewModel(
    private val repository: ItemListRepository
) : ViewModel() {

}