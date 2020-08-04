package com.yhwang.nicole.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yhwang.nicole.repository.Camera2DRepository

class Camera2DViewModel(
    private val repository: Camera2DRepository
) : ViewModel() {
    var noBgBitmap = MutableLiveData<Bitmap>()
    fun getNoBgBitMap(bitmap: Bitmap) {
        repository.getRemoveBgBitmap(noBgBitmap, bitmap)
    }

    fun saveScreenShot(bitmap: Bitmap, callback: ()->Unit) {
        repository.saveBitmapToJpg(bitmap, callback)
    }
}