package com.yhwang.nicole.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yhwang.nicole.model.Object2D
import com.yhwang.nicole.repository.ObjectListRepository
import timber.log.Timber

class ObjectListViewModel(
    private val repository: ObjectListRepository
) : ViewModel() {
    val objectDrawableList = repository.getObjectDrawableImage()

    var object2DList = MutableLiveData<ArrayList<Object2D>>()
    fun updateObject2DList() {
        Timber.i("update object 2d list")
        repository.getObjectList {
            object2DList.postValue(it)
        }
    }
    fun removeObject(obj: Object2D) {
        Timber.i("remove object 2d ${obj.id}")
        repository.removeObject(obj)
        updateObject2DList()
    }

    companion object {
        class Factory(
            private val objectListRepository: ObjectListRepository
        ) : ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return ObjectListViewModel(
                    objectListRepository
                ) as T
            }
        }
    }
}