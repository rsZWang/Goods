package com.yhwang.nicole.repository

import android.content.Context
import android.graphics.Bitmap
import com.yhwang.nicole.database.GoodsDatabase
import com.yhwang.nicole.model.Object2D
import com.yhwang.nicole.utility.bitmapToFile
import com.yhwang.nicole.utility.removeBg
import com.yhwang.nicole.utility.saveBitmapToGallery

class Object2DCameraRepository(
    private val context: Context,
    private val roomDatabase: GoodsDatabase
) {
    fun removeBg(origin: Bitmap, callback: (bitmap: Bitmap) -> Unit) {
        removeBg(context, origin, callback)
    }

    fun saveScreenToGallery(bitmap: Bitmap, callback: ()->Unit) {
        saveBitmapToGallery(context, bitmap, callback)
    }

    fun saveObjectAndBg(obj: Bitmap, x: Float, y: Float, background: Bitmap, callback: () -> Unit) {
        val objectName = "obj_2d_" + System.currentTimeMillis().toString() + ".png"
        val backgroundName = "obj_2d_" + System.currentTimeMillis().toString() + "_bg.jpeg"
        bitmapToFile(context, obj, objectName, Bitmap.CompressFormat.PNG)
        bitmapToFile(context, background, backgroundName, Bitmap.CompressFormat.JPEG)
        roomDatabase.object2DDao().insertObject(Object2D(objectName, x, y, backgroundName))
        callback()
    }
}