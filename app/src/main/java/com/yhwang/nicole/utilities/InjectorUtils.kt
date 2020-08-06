package com.yhwang.nicole.utilities

import android.content.Context
import com.theapache64.removebg.RemoveBg
import com.yhwang.nicole.GoodsDatabase
import com.yhwang.nicole.repository.Camera2DRepository
import com.yhwang.nicole.repository.ItemListRepository
import com.yhwang.nicole.viewModel.Camera2DViewModelFactory
import com.yhwang.nicole.viewModel.ItemListViewModelFactory

object InjectorUtils {
    fun provideCamera2DViewModelFactory(context: Context) : Camera2DViewModelFactory {
        RemoveBg.init("ESJkDD4fTYSiGpLZAqQ6rd2T")
        val repository = Camera2DRepository(context, GoodsDatabase.getInstance(context)!!)
        return Camera2DViewModelFactory(repository)
    }

    fun provideItemListViewModelFactory(context: Context) : ItemListViewModelFactory {
        val repository = ItemListRepository(context, GoodsDatabase.getInstance(context)!!)
        return ItemListViewModelFactory(repository)
    }
}