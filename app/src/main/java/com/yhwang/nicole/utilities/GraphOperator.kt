package com.yhwang.nicole.utilities

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Environment
import android.view.View
import android.widget.Toast
import timber.log.Timber
import java.io.File

fun layoutToBitmap(view: View): Bitmap {
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return bitmap
}

fun layoutToDrawable(resources: Resources, view: View): Drawable {
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return BitmapDrawable(resources, bitmap)
}

fun drawOutline(originalBitmap: Bitmap, color: Int) : Bitmap {
    val strokeWidth = 4
    val newStrokedBitmap = Bitmap.createBitmap(
        originalBitmap.width + 2 * strokeWidth,
        originalBitmap.height + 2 * strokeWidth,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(newStrokedBitmap)
    val scaleX =
        (originalBitmap.width + 2.0f * strokeWidth) / originalBitmap.width
    val scaleY =
        (originalBitmap.height + 2.0f * strokeWidth) / originalBitmap.height
    val matrix = Matrix()
    matrix.setScale(scaleX, scaleY)
    canvas.drawBitmap(originalBitmap, matrix, null)
    canvas.drawColor(
        color,
        PorterDuff.Mode.SRC_ATOP
    ) //Color.WHITE is stroke color

    canvas.drawBitmap(originalBitmap, strokeWidth.toFloat(), strokeWidth.toFloat(), null)
    return newStrokedBitmap
}