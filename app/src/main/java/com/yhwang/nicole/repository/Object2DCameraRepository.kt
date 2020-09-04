package com.yhwang.nicole.repository

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import com.theapache64.removebg.RemoveBg
import com.theapache64.removebg.utils.ErrorResponse
import com.yhwang.nicole.database.GoodsDatabase
import com.yhwang.nicole.model.Object2D
import com.yhwang.nicole.utility.bitmapToFile
import com.yhwang.nicole.utility.saveBitmapToGallery
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Object2DCameraRepository(
    private val context: Context,
    private val roomDatabase: GoodsDatabase
) {
    fun removeBg(origin: Bitmap, callback: (bitmap: Bitmap) -> Unit) {
        val imageFile = File(context.cacheDir, "capturedImage.jpeg")
        try {
            val outputStream = FileOutputStream(imageFile)
            origin.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        RemoveBg.from(imageFile, object : RemoveBg.RemoveBgCallback {
            override fun onError(errors: List<ErrorResponse.Error>) {
                AlertDialog.Builder(context)
                    .setMessage(errors.toString())
                    .setCancelable(true)
                    .show()
            }
            override fun onProcessing() { }
            override fun onUploadProgress(progress: Float) { }
            override fun onSuccess(bitmap: Bitmap) {
                Timber.d("remove bg successfully")
                callback(bitmap)
            }
        })
    }

    fun saveScreenToGallery(bitmap: Bitmap, callback: ()->Unit) {
        saveBitmapToGallery(context, bitmap, callback)
    }

    fun saveObjectAndBg(obj: Bitmap, x: Float, y: Float, background: Bitmap, callback: () -> Unit) {
        val objectName = "object_2d_" + System.currentTimeMillis().toString() + ".png"
        val backgroundName = "object_2d_bg_" + System.currentTimeMillis().toString() + ".jpeg"
        bitmapToFile(context, obj, objectName, Bitmap.CompressFormat.PNG)
        bitmapToFile(context, background, backgroundName, Bitmap.CompressFormat.JPEG)
        roomDatabase.object2DDao().insertObject(Object2D(objectName, x, y, backgroundName))
        callback()
    }
}