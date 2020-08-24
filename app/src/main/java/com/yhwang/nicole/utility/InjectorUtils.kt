package com.yhwang.nicole.utility

import android.content.Context
import com.yhwang.nicole.database.GoodsDatabase
import com.yhwang.nicole.repository.Camera2DRepository
import com.yhwang.nicole.repository.ItemListRepository
import com.yhwang.nicole.repository.ItemRepository
import com.yhwang.nicole.viewModel.Camera2DViewModelFactory
import com.yhwang.nicole.viewModel.ItemListViewModelFactory
import com.yhwang.nicole.viewModel.ItemDetailViewModel

object InjectorUtils {
    fun provideCamera2DViewModelFactory(context: Context) : Camera2DViewModelFactory {
        val repository = Camera2DRepository(context, GoodsDatabase.getInstance(context)!!)
        return Camera2DViewModelFactory(repository)
    }

    fun provideItemListViewModelFactory(context: Context) : ItemListViewModelFactory {
        val repository = ItemListRepository(context, GoodsDatabase.getInstance(context)!!)
        return ItemListViewModelFactory(repository)
    }

    fun provideItemViewModelFactory(context: Context) : ItemDetailViewModel.ItemViewModelFactory {
        val repository = ItemRepository(context)
        return ItemDetailViewModel.ItemViewModelFactory(repository)
    }
}