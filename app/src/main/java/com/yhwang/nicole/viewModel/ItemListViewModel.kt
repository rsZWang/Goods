package com.yhwang.nicole.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yhwang.nicole.model.Item
import com.yhwang.nicole.repository.ItemListRepository
import kotlinx.coroutines.*
import timber.log.Timber

class ItemListViewModel(
    private val repository: ItemListRepository
) : ViewModel() {
    val itemDrawableList = repository.getItemDrawableImage()

    var itemList = MutableLiveData<ArrayList<Item>>()
    fun updateItemList() {
        Timber.i("update item list")
        repository.getItemList {
            itemList.postValue(it)
        }
    }
    fun removeItem(item: Item) {
        Timber.i("remove item ${item.id}")
        repository.removeItem(item)
        updateItemList()
    }
}