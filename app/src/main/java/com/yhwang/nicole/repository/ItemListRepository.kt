package com.yhwang.nicole.repository

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.yhwang.nicole.GoodsDatabase
import com.yhwang.nicole.R
import com.yhwang.nicole.model.Item
import com.yhwang.nicole.utilities.deleteImageFile

class ItemListRepository(
    val context: Context,
    private val room: GoodsDatabase
) {
    fun getItemDrawableImage(): LiveData<ArrayList<Drawable>> {
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

    fun getItemList(callback: (ArrayList<Item>)->Unit) {
        Thread {
            callback(ArrayList(room.itemDao().getAllItem()))
        }.start()
    }

    fun removeItem(item: Item) {
        room.itemDao().deleteItem(item)
        deleteImageFile(context, item.itemFileName)
        deleteImageFile(context, item.backgroundFileName)
    }
}