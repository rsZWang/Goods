package com.yhwang.nicole.viewModel

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yhwang.nicole.repository.Camera2DRepository

class Camera2DViewModel(
    private val repository: Camera2DRepository
) : ViewModel() {
    fun removeBg(bitmap: Bitmap, callback: (bitmap: Bitmap) -> Unit) =
        repository.removeBg(bitmap, callback)

    fun saveScreenToGallery(bitmap: Bitmap, callback: ()->Unit) =
        repository.saveScreenToGallery(bitmap, callback)

    fun saveItemAndBg(itemBitmap: Bitmap, x: Float, y: Float, background: Bitmap, callback: () -> Unit) =
        repository.saveItemAndBg(
            itemBitmap,
            x,
            y,
            background,
            callback)
}