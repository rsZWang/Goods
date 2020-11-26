package com.yhwang.nicole.viewModel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yhwang.nicole.repository.Object2DCameraRepository
import kotlin.concurrent.thread

class Object2DCameraViewModel(
    private val cameraRepository: Object2DCameraRepository
) : ViewModel() {
    fun removeBg(bitmap: Bitmap, callback: (bitmap: Bitmap) -> Unit) =
        cameraRepository.removeBg(bitmap, callback)

    fun saveScreenToGallery(bitmap: Bitmap, callback: (Uri)->Unit) {
        thread {
            cameraRepository.saveScreenToGallery(bitmap, callback)
        }
    }

    fun saveObjectAndBg(objectBitmap: Bitmap, x: Float, y: Float, background: Bitmap, callback: () -> Unit) {
        thread {
            cameraRepository.saveObjectAndBg(
                objectBitmap,
                x,
                y,
                background,
                callback
            )
        }
    }
}

class Object2DCameraViewModelFactory(
    private val object2DCameraRepository: Object2DCameraRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return Object2DCameraViewModel(
            object2DCameraRepository
        ) as T
    }
}