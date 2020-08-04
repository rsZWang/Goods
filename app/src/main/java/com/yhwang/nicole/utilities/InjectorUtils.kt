package com.yhwang.nicole.utilities

import android.content.Context
import com.theapache64.removebg.RemoveBg
import com.yhwang.nicole.repository.Camera2DRepository
import com.yhwang.nicole.viewModel.Camera2DViewModelFactory

object InjectorUtils {
    fun provideCamera2DViewModeFactory(context: Context): Camera2DViewModelFactory {
        RemoveBg.init("ESJkDD4fTYSiGpLZAqQ6rd2T")
        val repository = Camera2DRepository(context)
        return Camera2DViewModelFactory(repository)
    }
}