package com.yhwang.nicole.utility

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.View
import androidx.core.content.ContextCompat
import com.yhwang.nicole.R
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
    val strokeWidth = 20
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

/**
 * Covert dp to px
 * @param dp
 * @param context
 * @return pixel
 */
fun dpToPixel(context: Context, dp: Float): Float {
    return dp * getDensity(context)
}

/**
 * Covert px to dp
 * @param px
 * @param context
 * @return dp
 */
fun pixelToDp( context: Context, px: Float): Float {
    return px / getDensity(context)
}

/**
 * Get screen destiny
 * 120dpi = 0.75
 * 160dpi = 1 (default)
 * 240dpi = 1.5
 * @param context
 * @return
 */
fun getDensity(context: Context): Float {
    val metrics: DisplayMetrics = context.resources.displayMetrics
    return metrics.density
}

fun trimTransparentPart(bitmap: Bitmap): Bitmap {
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

fun cropBitmapTransparency(sourceBitmap: Bitmap): Bitmap {
    var minX = sourceBitmap.width
    var minY = sourceBitmap.height
    var maxX = -1
    var maxY = -1
    for (y in 0 until sourceBitmap.height) {
        for (x in 0 until sourceBitmap.width) {
            val alpha = sourceBitmap.getPixel(x, y) shr 24 and 255
            if (alpha > 0) // pixel is not 100% transparent
            {
                if (x < minX) minX = x
                if (x > maxX) maxX = x
                if (y < minY) minY = y
                if (y > maxY) maxY = y
            }
        }
    }
    return Bitmap.createBitmap(
        sourceBitmap,
        minX,
        minY,
        maxX - minX + 1,
        maxY - minY + 1
    )
}

fun highlight(context: Context, src: Bitmap): Bitmap {
    val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    canvas.drawColor(0, PorterDuff.Mode.CLEAR)
    val paintBlur = Paint()
    paintBlur.maskFilter = BlurMaskFilter(30F, BlurMaskFilter.Blur.NORMAL)
    val offsetXY = IntArray(2)
    val alpha = src.extractAlpha(paintBlur, offsetXY)
    val ptAlphaColor = Paint()
    ptAlphaColor.color = ContextCompat.getColor(context, R.color.objectBlue)
    canvas.drawBitmap(alpha, offsetXY[0].toFloat(), offsetXY[1].toFloat(), ptAlphaColor)
    alpha.recycle()
    canvas.drawBitmap(src, 0F, 0F, null)
    return output
}