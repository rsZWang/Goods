package com.yhwang.nicole.utilities

import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

fun saveBitmapToGallery(context: Context, bitmap: Bitmap, callback: ()->Unit) {
    if (Build.VERSION.SDK_INT >= 29) {
        val values = contentValues()
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Goods")
        values.put(MediaStore.Video.Media.DISPLAY_NAME, System.currentTimeMillis().toString() + ".jpg")
        values.put(MediaStore.Images.Media.IS_PENDING, true)
        // RELATIVE_PATH and IS_PENDING are introduced in API 29.

        val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            writeBitmapToStream(bitmap, context.contentResolver.openOutputStream(uri))
            values.put(MediaStore.Images.Media.IS_PENDING, false)
            context.contentResolver.update(uri, values, null, null)
        }
    } else {
        val directory = File(Environment.getExternalStorageDirectory().toString() + File.separator + "RemoveBg")
        if (!directory.exists()) { directory.mkdirs() }
        val fileName = System.currentTimeMillis().toString() + ".jpg"
        val file = File(directory, fileName)
        writeBitmapToStream(bitmap, FileOutputStream(file))
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

private fun writeBitmapToStream(bitmap: Bitmap, outputStream: OutputStream?) {
    if (outputStream != null) {
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun fileToBitmap(context: Context, fileName: String) : Bitmap {
    var file = ContextWrapper(context).getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE)
    file = File(file, fileName)
    return BitmapFactory.decodeFile(file.path)
}

fun bitmapToFile(context: Context, bitmap: Bitmap, fileName: String, format: Bitmap.CompressFormat) {
    // Initialize a new file instance to save bitmap object
    var file = ContextWrapper(context).getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE)
    file = File(file, fileName)

    try {
        // Compress the bitmap and save in jpg format
        val stream: OutputStream = FileOutputStream(file)
        bitmap.compress(format, 100, stream)
        stream.flush()
        stream.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}