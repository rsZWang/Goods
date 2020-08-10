package com.yhwang.nicole.utility

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import java.util.*


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

fun trimTransparentPart(bitmap: Bitmap): Bitmap? {
    val height = bitmap.height
    val width = bitmap.width
    var top = 0
    var left = 0
    var bottom = height
    var right = width
    var empty = IntArray(width)
    var buffer = IntArray(width)
    Arrays.fill(empty, 0)
    for (y in 0 until height) {
        bitmap.getPixels(buffer, 0, width, 0, y, width, 1)
        if (!empty.contentEquals(buffer)) {
            top = y
            break
        }
    }
    for (y in height - 1 downTo top + 1) {
        bitmap.getPixels(buffer, 0, width, 0, y, width, 1)
        if (!empty.contentEquals(buffer)) {
            bottom = y
            break
        }
    }

    val bufferSize = bottom - top + 1
    empty = IntArray(bufferSize)
    buffer = IntArray(bufferSize)
    Arrays.fill(empty, 0)
    for (x in 0 until width) {
        bitmap.getPixels(buffer, 0, 1, x, top + 1, 1, bufferSize)
        if (!empty.contentEquals(buffer)) {
            left = x
            break
        }
    }
    Arrays.fill(empty, 0)
    for (x in width - 1 downTo left + 1) {
        bitmap.getPixels(buffer, 0, 1, x, top + 1, 1, bufferSize)
        if (!empty.contentEquals(buffer)) {
            right = x
            break
        }
    }
    return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
}
