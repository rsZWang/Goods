package com.yhwang.nicole.viewModel

import android.content.res.AssetManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yhwang.nicole.model.Object2D
import com.yhwang.nicole.repository.ObjectListRepository
import com.yhwang.nicole.view.ObjectListFragment
import timber.log.Timber
import javax.security.auth.callback.Callback

class ObjectListViewModel(
    private val repository: ObjectListRepository
) : ViewModel() {

    var object2DList = MutableLiveData<ArrayList<Object2D>>()
    fun loadObject2DList() {
        Timber.i("Load object 2D list")
        repository.getObject2DList {
            object2DList.postValue(it)
        }
    }
    fun removeObject2D(obj: Object2D) {
        Timber.i("remove object 2D ${obj.id}")
        repository.removeObject2D(obj)
        loadObject2DList()
    }

    fun updateObject2D(assetList: Array<String>, callback: () -> Unit) {
        Timber.i("Update object 2D list")
        repository.getObject2DNameList { dbList ->
            for (assetFile in assetList) {
                if (assetFile.contains("nobg") && !dbList.contains(assetFile)) {
                    repository.saveAssetsObject2D(
                        objectName = assetFile,
                        backgroundName = "${assetFile.removeSuffix("_nobg.png")}.jpg"
                    )
                }
            }
            callback()
        }
    }

    var object3DList = MutableLiveData<ArrayList<String>>()
    fun loadObject3DList(assetManager: AssetManager) {
        Timber.i("Load object 2D list")
        object3DList.postValue(repository.getObject3DList(assetManager))
    }

    var objectList = MutableLiveData<List<ObjectListFragment.Object>>()
    fun loadObjectList(appInList: Array<String>, assetManager: AssetManager) {
        val list = ArrayList<ObjectListFragment.Object>()
        updateObject2D(appInList) {
            Timber.i("Load object 2D list")
            repository.getObject2DList { object2DList ->
                Timber.i("Get object 2D list: ${object2DList.size}")
                for (object2D in object2DList) {
                    list.add(ObjectListFragment.Object(
                        ObjectListFragment.ObjectDimension.Object2D,
                        object2D = object2D
                    ))
                }
                for (object3D in repository.getObject3DList(assetManager)) {
                    list.add(ObjectListFragment.Object(
                        ObjectListFragment.ObjectDimension.Object3D,
                        object3D = object3D
                    ))
                }
                objectList.postValue(list)
            }
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