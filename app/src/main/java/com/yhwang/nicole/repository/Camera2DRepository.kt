package com.yhwang.nicole.repository

import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import com.theapache64.removebg.RemoveBg
import com.theapache64.removebg.utils.ErrorResponse
import com.yhwang.nicole.GoodsDatabase
import com.yhwang.nicole.model.Item
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class Camera2DRepository(
    private val context: Context,
    private val roomDatabase: GoodsDatabase
) {
    fun getRemoveBgBitmap(bitMapLiveData: MutableLiveData<Bitmap>, bitmap: Bitmap) {
        val imageFile = File(context.cacheDir, "capturedImage.jpeg")
        try {
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
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

    fun saveBitmapToGallery(bitmap: Bitmap, callback: ()->Unit) {
        if (Build.VERSION.SDK_INT >= 29) {
            val values = contentValues()
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Goods")
            values.put(MediaStore.Video.Media.DISPLAY_NAME, System.currentTimeMillis().toString() + ".jpg")
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            // RELATIVE_PATH and IS_PENDING are introduced in API 29.

            val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
                Timber.d("uri %s", uri.path)
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)
            }
        } else {
            val directory = File(Environment.getExternalStorageDirectory().toString() + File.separator + "RemoveBg")
            if (!directory.exists()) { directory.mkdirs() }
            val fileName = System.currentTimeMillis().toString() + ".jpg"
            val file = File(directory, fileName)
            saveImageToStream(bitmap, FileOutputStream(file))
            val values = contentValues()
            values.put(MediaStore.Images.Media.DATA, file.absolutePath)
            // .DATA is deprecated in API 29
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        }
        callback()
    }

    private fun contentValues() : ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        return values
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveItemAndBg(item: Bitmap, x: Float, y: Float, margin: List<Int>, background: Bitmap, callback: () -> Unit) {
        val itemName = "item_" + System.currentTimeMillis().toString() + ".jpg"
        val itemBackgroundName = "item_bg_" + System.currentTimeMillis().toString() + ".jpg"
        saveBitmapToFile(item, itemName, Bitmap.CompressFormat.PNG)
        saveBitmapToFile(background, itemBackgroundName, Bitmap.CompressFormat.JPEG)
        roomDatabase.itemDao().insertItem(Item(itemName, x, y, margin, itemBackgroundName))
        callback()
    }

    private fun saveBitmapToFile(bitmap: Bitmap, fileName: String, format: Bitmap.CompressFormat) {
        // Initialize a new file instance to save bitmap object
        var file = ContextWrapper(context).getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE)
        file = File(file, "$fileName.jpg")

        try {
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(format, 100, stream)
            stream.flush()
            stream.close()
        } catch (e:IOException) {
            e.printStackTrace()
        }
    }
}