package com.yhwang.nicole.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yhwang.nicole.repository.Camera2DRepository

class Camera2DViewModelFactory(
    private val camera2DRepository: Camera2DRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return Camera2DViewModel(
            camera2DRepository
        ) as T
    }
}