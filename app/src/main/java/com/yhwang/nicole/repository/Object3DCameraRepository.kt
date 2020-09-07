package com.yhwang.nicole.repository

import android.content.Context
import android.graphics.Bitmap
import com.yhwang.nicole.utility.bitmapToFile

class Object3DCameraRepository(
    val context: Context
) {
    fun saveObjectAndBg(original: Bitmap, obj: Bitmap, background: Bitmap, callback: () -> Unit) {
        val objName = "object_3d_" + System.currentTimeMillis().toString() + ".png"
        val backgroundName = "object_3d_" + System.currentTimeMillis().toString() + "_bg.jpeg"
        val originalName = "object_3d_" +  System.currentTimeMillis().toString() + "_original.jpeg"
        bitmapToFile(context, obj, objName, Bitmap.CompressFormat.PNG)
        bitmapToFile(context, background, backgroundName, Bitmap.CompressFormat.JPEG)
        bitmapToFile(context, original, originalName, Bitmap.CompressFormat.JPEG)
        callback()
    }
}