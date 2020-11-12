package com.yhwang.nicole.repository

import android.content.Context
import android.content.res.AssetManager
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.yhwang.nicole.database.GoodsDatabase
import com.yhwang.nicole.R
import com.yhwang.nicole.model.Object2D
import com.yhwang.nicole.utility.deleteImageFile
import timber.log.Timber
import kotlin.concurrent.thread

class ObjectListRepository(
    private val context: Context,
    private val room: GoodsDatabase
) {
    fun getObjectDrawableImage(): LiveData<ArrayList<Drawable>> {
        val mutableLiveData = MutableLiveData<ArrayList<Drawable>>()
        val list = ArrayList<Drawable>()
        for (i in 0..1) {
            list.add(ContextCompat.getDrawable(context, R.drawable.pikachu_1)!!)
            list.add(ContextCompat.getDrawable(context, R.drawable.pikachu_2)!!)
            list.add(ContextCompat.getDrawable(context, R.drawable.pikachu_3)!!)
        }
        mutableLiveData.postValue(list)

        return mutableLiveData
    }

    fun getObject2DList(callback: (ArrayList<Object2D>)->Unit) {
        thread {
            callback(ArrayList(room.object2DDao().getAllObject()))
        }
    }

    fun getObject2DNameList(callback: (List<String>) -> Unit) {
        thread {
            callback(room.object2DDao().getAllObjectName())
        }
    }

    fun removeObject2D(object2D: Object2D) {
        room.object2DDao().deleteObject(object2D)
        deleteImageFile(context, object2D.objectFileName)
        deleteImageFile(context, object2D.backgroundFileName)
    }

    fun saveAssetsObject2D(objectName: String, backgroundName: String) {
        Timber.i("Object name: $objectName")
        Timber.i("Object bg name: $backgroundName")
        room.object2DDao().insertObject(Object2D(
            objectFileName = objectName,
            0f,
            0f,
            backgroundFileName = backgroundName,
            isAsset = true)
        )
    }

    fun getObject3DList(assetManager: AssetManager) : ArrayList<String> {
        val nameList = ArrayList<String>()
        val list = assetManager.list("object")!!
        for (fileName in list) {
            var name = fileName.substringAfterLast("3d_")
            name = name.substringBefore("_")
            name = name.substringBefore(".")
            if (!nameList.contains(name)) {
                nameList.add(name)
            }
        }
        return nameList
    }
}