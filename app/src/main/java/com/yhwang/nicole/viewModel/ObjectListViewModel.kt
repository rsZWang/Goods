package com.yhwang.nicole.viewModel

import android.content.res.AssetManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yhwang.nicole.model.Object2D
import com.yhwang.nicole.repository.ObjectListRepository
import timber.log.Timber
import javax.security.auth.callback.Callback

class ObjectListViewModel(
    private val repository: ObjectListRepository
) : ViewModel() {

    var object2DList = MutableLiveData<ArrayList<Object2D>>()
    fun updateObject2DList() {
        Timber.i("update object 2d list")
        repository.getObject2DList {
            object2DList.postValue(it)
        }
    }
    fun removeObject2D(obj: Object2D) {
        Timber.i("remove object 2d ${obj.id}")
        repository.removeObject2D(obj)
        updateObject2DList()
    }

    var object3DList = MutableLiveData<ArrayList<String>>()
    fun getObject3DList(assetManager: AssetManager) {
        object3DList.postValue(repository.getObject3DList(assetManager))
    }

    fun updateObject2D(list: Array<String>, callback: () -> Unit) {
        repository.getObject2DNameList { nameList ->
            for (file in list) {
                if (!nameList.contains(file) && !file.contains("nobg")) {
                    Timber.i("Save $file")
                    repository.saveAssetsObject2D("${file.removeSuffix(".jpg")}_nobg.png", file)
                }
            }
            callback()
        }
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