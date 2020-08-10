package com.yhwang.nicole.repository

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import com.theapache64.removebg.RemoveBg
import com.theapache64.removebg.utils.ErrorResponse
import com.yhwang.nicole.database.GoodsDatabase
import com.yhwang.nicole.model.Item
import com.yhwang.nicole.utility.bitmapToFile
import com.yhwang.nicole.utility.saveBitmapToGallery
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Camera2DRepository(
    private val context: Context,
    private val roomDatabase: GoodsDatabase
) {
    fun getRemoveBgBitmap(bitMapLiveData: MutableLiveData<Bitmap>, bitmap: Bitmap) {
        val imageFile = File(context.cacheDir, "capturedImage.jpeg")
        try {
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        RemoveBg.from(imageFile, object : RemoveBg.RemoveBgCallback {
            override fun onError(errors: List<ErrorResponse.Error>) { }
            override fun onProcessing() { }
            override fun onUploadProgress(progress: Float) { }
            override fun onSuccess(bitmap: Bitmap) {
                Timber.d("remove bg successfully")
                bitMapLiveData.postValue(bitmap)
            }
        })
    }

    fun saveScreenToGallery(bitmap: Bitmap, callback: ()->Unit) {
        saveBitmapToGallery(context, bitmap, callback)
    }

    fun saveItemAndBg(item: Bitmap, background: Bitmap, callback: () -> Unit) {
        val itemName = "item_" + System.currentTimeMillis().toString() + ".png"
        val backgroundName = "item_bg_" + System.currentTimeMillis().toString() + ".jpeg"
        bitmapToFile(context, item, itemName, Bitmap.CompressFormat.PNG)
        bitmapToFile(context, background, backgroundName, Bitmap.CompressFormat.JPEG)
        roomDatabase.itemDao().insertItem(Item(itemName, backgroundName))
        callback()
    }
}