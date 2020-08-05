package com.yhwang.nicole.viewModel

import androidx.lifecycle.ViewModel
import com.yhwang.nicole.repository.ItemListRepository

class ItemListViewModel(
    repository: ItemListRepository
) : ViewModel() {
    val itemDrawableList = repository.getItemDrawableImage()
}