package com.yhwang.nicole.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yhwang.nicole.model.Item
import com.yhwang.nicole.repository.ItemListRepository

class ItemListViewModel(
    private val repository: ItemListRepository
) : ViewModel() {
    val itemDrawableList = repository.getItemDrawableImage()

    fun getItemList() : LiveData<List<Item>> {
        val liveData = MutableLiveData<List<Item>>()
        repository.getItemImages(liveData)
        return liveData
    }
}