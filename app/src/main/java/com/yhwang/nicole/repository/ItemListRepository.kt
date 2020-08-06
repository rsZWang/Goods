package com.yhwang.nicole.repository

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.yhwang.nicole.GoodsDatabase
import com.yhwang.nicole.R
import com.yhwang.nicole.model.Item

class ItemListRepository(
    val context: Context,
    private val roomDatabase: GoodsDatabase
) {
    fun getItemDrawableImage(): LiveData<ArrayList<Drawable>> {
        val mutableLiveData = MutableLiveData<ArrayList<Drawable>>()
        val list = ArrayList<Drawable>()
        for (i in 0..4) {
            list.add(ContextCompat.getDrawable(context, R.drawable.pikachu_1)!!)
            list.add(ContextCompat.getDrawable(context, R.drawable.pikachu_2)!!)
            list.add(ContextCompat.getDrawable(context, R.drawable.pikachu_3)!!)
        }
        mutableLiveData.postValue(list)

        return mutableLiveData
    }

    fun getItemImages() : LiveData<List<Item>> {
        val listItemLiveData = MutableLiveData<List<Item>>()
        val itemList = roomDatabase.itemDao().getAllItem()
        if (roomDatabase.itemDao().getAllItem().isNotEmpty()) {
            listItemLiveData.postValue(itemList)
        }
        return listItemLiveData
    }
}