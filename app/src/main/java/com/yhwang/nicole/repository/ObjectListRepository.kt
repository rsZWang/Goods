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

class ObjectListRepository(
    val context: Context,
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
        Thread {
            callback(ArrayList(room.object2DDao().getAllObject()))
        }.start()
    }

    fun removeObject2D(object2D: Object2D) {
        room.object2DDao().deleteObject(object2D)
        deleteImageFile(context, object2D.objectFileName)
        deleteImageFile(context, object2D.backgroundFileName)
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