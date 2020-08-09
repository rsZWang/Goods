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
    var noBgBitmap = MutableLiveData<Bitmap>()
    fun getNoBgBitMap(bitmap: Bitmap) =
        repository.getRemoveBgBitmap(noBgBitmap, bitmap)

    fun saveScreenToGallery(bitmap: Bitmap, callback: ()->Unit) =
        repository.saveScreenToGallery(bitmap, callback)

    fun saveItemAndBg(itemBitmap: Bitmap, background: Bitmap, callback: () -> Unit) =
        repository.saveItemAndBg(
            itemBitmap,
            background,
            callback)
}