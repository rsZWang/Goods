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
    fun getNoBgBitMap(bitmap: Bitmap) {
        repository.getRemoveBgBitmap(noBgBitmap, bitmap)
    }

    fun saveScreenShot(bitmap: Bitmap, callback: ()->Unit) =
        repository.saveBitmapToGallery(bitmap, callback)


    fun saveItem(itemView: View, background: Bitmap, callback: () -> Unit) {

        val width = itemView.layoutParams.width
        val height = itemView.layoutParams.height

        val itemBitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(itemBitmap)
        itemView.draw(canvas)

        val arrayList = ArrayList<Int>()
        arrayList.add(itemView.marginTop)
        arrayList.add(itemView.marginBottom)
        arrayList.add(itemView.marginStart)
        arrayList.add(itemView.marginEnd)

        repository.saveItemAndBg(
            itemBitmap,
            itemView.x,
            itemView.y,
            arrayList,
            background,
            callback)
    }
}