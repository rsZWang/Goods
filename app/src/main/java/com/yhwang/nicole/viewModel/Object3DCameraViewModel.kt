package com.yhwang.nicole.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yhwang.nicole.repository.Object3DCameraRepository

class Object3DCameraViewModel(
    private val repository: Object3DCameraRepository
) : ViewModel() {
    fun saveObjectAndBg(original: Bitmap, obj: Bitmap, background: Bitmap, callback: () -> Unit) =
        repository.saveObjectAndBg(
            original,
            obj,
            background,
            callback)

    companion object {
        class Factory(
            private val object3DCameraRepository: Object3DCameraRepository
        ) : ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return Object3DCameraViewModel(
                    object3DCameraRepository
                ) as T
            }
        }
    }
}