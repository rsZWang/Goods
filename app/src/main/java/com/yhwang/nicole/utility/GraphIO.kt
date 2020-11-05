package com.yhwang.nicole.utility

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import com.theapache64.removebg.RemoveBg
import com.theapache64.removebg.utils.ErrorResponse
import com.yhwang.nicole.model.Object2D
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

fun saveBitmapToGallery(context: Context, bitmap: Bitmap, callback: (Uri)->Unit) {
    if (Build.VERSION.SDK_INT >= 29) {
        val imageContentValues = getImageContentValues(null)
        val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageContentValues)
        if (uri != null) {
            writeBitmapToStream(bitmap, context.contentResolver.openOutputStream(uri))
            imageContentValues.put(MediaStore.Images.Media.IS_PENDING, false)
            context.contentResolver.update(uri, imageContentValues, null, null)
            callback(uri)
        }
    } else {
        val directory = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Goods")
        if (!directory.exists()) { directory.mkdirs() }
        val fileName = System.currentTimeMillis().toString() + ".jpg"
        val file = File(directory, fileName)
        writeBitmapToStream(bitmap, FileOutputStream(file))
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, getImageContentValues(file.absolutePath))
        callback(Uri.parse(file.absolutePath))
    }
}

private fun getImageContentValues(filePath: String?) : ContentValues {
    val values = ContentValues()
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
    values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
    if (Build.VERSION.SDK_INT >= 29) {
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Goods")
        values.put(MediaStore.Video.Media.DISPLAY_NAME, System.currentTimeMillis().toString() + ".jpg")
//        values.put(MediaStore.Images.Media.IS_PENDING, true)
//         RELATIVE_PATH and IS_PENDING are introduced in API 29.
    } else {
        values.put(MediaStore.Images.Media.DATA, filePath)
//         .DATA is deprecated in API 29
    }
    return values
}

private fun writeBitmapToStream(bitmap: Bitmap, outputStream: OutputStream?) {
    if (outputStream != null) {
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
        } catch (e: Exception) {
            throw e
        }
    }
}

fun assetsImageToBitmap(assetManager: AssetManager, fileName: String) : Bitmap {
    Timber.i("asset image name: obj_3d_${fileName}")
    val inputStream = assetManager.open("object/obj_3d_${fileName}")
    return BitmapFactory.decodeStream(inputStream)
}

fun fileToBitmap(context: Context, object2D: Object2D, isBackground: Boolean) : Bitmap {
    return if (object2D.isAsset) {
        BitmapFactory.decodeStream(context.assets.open("object_2d/${ if (isBackground) object2D.backgroundFileName else object2D.objectFileName }"))
    } else {
        var file = ContextWrapper(context).getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE)
        file = File(file, if (isBackground) object2D.backgroundFileName else object2D.objectFileName )
        BitmapFactory.decodeFile(file.path)
    }
}

fun bitmapToFile(context: Context, bitmap: Bitmap, fileName: String, format: Bitmap.CompressFormat) {
    var file = ContextWrapper(context).getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE)
    file = File(file, fileName)
    try {
        val stream: OutputStream = FileOutputStream(file)
        bitmap.compress(format, 100, stream)
        stream.flush()
        stream.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun deleteImageFile(context: Context, fileName: String) {
    if (File(
        ContextWrapper(context).getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE),
        fileName
    ).delete()) {
        Timber.i("successfully delete $fileName")
    } else {
        Timber.i("fail to delete $fileName")
    }
}

fun removeBg(context: Context, origin: Bitmap, callback: (bitmap: Bitmap) -> Unit) {
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

fun share(activity: Activity, uri: Uri) {
    val shareIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        type = "image/jpeg"
    }
    activity.startActivity(Intent.createChooser(shareIntent, "Share Image"))
}

//fun loadImageFileByPicasso(context: Context, fileName: String, target: ImageView) {
//    val path = "file://${ContextWrapper(context).getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE).path}/$fileName"
//    Picasso.get()
//        .load(path)
//        .noPlaceholder()
//        .into(target)
//}