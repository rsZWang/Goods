package com.yhwang.nicole.utility

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout


class ClipArt(paramContext: Context) : RelativeLayout(paramContext) {
//    var baseh = 0
//    var basew = 0
//    var basex: Int
//    var basey: Int
//    var btndel: ImageButton
//    var btnrot: ImageButton
//    var btnscl: ImageButton
//    var clip: RelativeLayout? = null
//    var cntx: Context
//    var freeze = false
//    var h = 0
//    var i = 0
//    var image: ImageView
//    var imageUri: String? = null
//    var isShadow = false
//    var iv = 0
//    var layBg: RelativeLayout? = null
//    var layGroup: RelativeLayout
//    var layoutParams: LayoutParams
//    var mInflater: LayoutInflater
//    var margl = 0
//    var margt = 0
//    var opacity = 1.0f
//    var originalBitmap: Bitmap? = null
//    var pivx: Int
//    var pivy: Int
//    var pos = 0
//    var shadowBitmap: Bitmap? = null
//    var startDegree = 0f
//    var v: Array<String>
//    fun disableAll() {
//        btndel.visibility = 4
//        btnrot.visibility = 4
//        btnscl.visibility = 4
//    }
//
//    val imageView: ImageView
//        get() = image
//
//    fun setFreeze(paramBoolean: Boolean) {
//        freeze = paramBoolean
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    @SuppressLint("ClickableViewAccessibility") init {
//        cntx = paramContext
//        layGroup = this
//        basex = 0
//        basey = 0
//        pivx = 0
//        pivy = 0
//        mInflater = paramContext.getSystemService("layout_inflater")
//        mInflater.inflate(R.layout.clipart, this, true)
//        btndel = findViewById<View>(R.id.del) as ImageButton
//        btnrot = findViewById<View>(R.id.rotate) as ImageButton
//        btnscl = findViewById<View>(R.id.sacle) as ImageButton
//        layoutParams = LayoutParams(250, 250)
//        layGroup.layoutParams = layoutParams
//        image = findViewById<View>(R.id.clipart) as ImageView
//        image.setImageResource(R.drawable.ic_launcher)
//        setOnTouchListener(object : OnTouchListener() {
//            val gestureDetector = GestureDetector(
//                cntx,
//                object : SimpleOnGestureListener() {
//                    override fun onDoubleTap(paramAnonymous2MotionEvent: MotionEvent): Boolean {
//                        return false
//                    }
//                })
//
//            override fun onTouch(paramAnonymousView: View?, event: MotionEvent): Boolean {
//                if (!freeze) {
//                    when (event.action) {
//                        MotionEvent.ACTION_DOWN -> {
//                            layGroup.invalidate()
//                            gestureDetector.onTouchEvent(event)
//                            layGroup.performClick()
//                            basex = (event.rawX - layoutParams.leftMargin).toInt()
//                            basey = (event.rawY - layoutParams.topMargin).toInt()
//                        }
//                        MotionEvent.ACTION_MOVE -> {
//                            val i = event.rawX.toInt()
//                            val j = event.rawY.toInt()
//                            layBg = parent as RelativeLayout
//                            if (i - basex > -(layGroup.width * 2 / 3)
//                                && i - basex < layBg!!.width - layGroup.width / 3
//                            ) {
//                                layoutParams.leftMargin = i - basex
//                            }
//                            if (j - basey > -(layGroup.height * 2 / 3)
//                                && j - basey < layBg!!.height - layGroup.height / 3
//                            ) {
//                                layoutParams.topMargin = j - basey
//                            }
//                            layoutParams.rightMargin = -1000
//                            layoutParams.bottomMargin = -1000
//                            layGroup.layoutParams = layoutParams
//                        }
//                    }
//                    return true
//                }
//                return true
//            }
//        })
//        btnscl.setOnTouchListener(OnTouchListener { paramAnonymousView, event ->
//            if (!freeze) {
//                var j = event.rawX.toInt()
//                var i = event.rawY.toInt()
//                layoutParams = layGroup.layoutParams as LayoutParams
//                when (event.action) {
//                    MotionEvent.ACTION_DOWN -> {
//                        layGroup.invalidate()
//                        basex = j
//                        basey = i
//                        basew = layGroup.width
//                        baseh = layGroup.height
//                        val loaction = IntArray(2)
//                        layGroup.getLocationOnScreen(loaction)
//                        margl = layoutParams.leftMargin
//                        margt = layoutParams.topMargin
//                    }
//                    MotionEvent.ACTION_MOVE -> {
//                        val f2 = Math.toDegrees(
//                            Math.atan2(
//                                i - basey.toDouble(),
//                                j - basex.toDouble()
//                            )
//                        ).toFloat()
//                        var f1 = f2
//                        if (f2 < 0.0f) {
//                            f1 = f2 + 360.0f
//                        }
//                        j -= basex
//                        var k = i - basey
//                        i = (Math.sqrt(j * j + k * k.toDouble())
//                                * Math.cos(
//                            Math.toRadians(
//                                f1 - layGroup.rotation.toDouble()
//                            )
//                        )).toInt()
//                        j = (Math.sqrt(i * i + k * k.toDouble())
//                                * Math.sin(
//                            Math.toRadians(
//                                f1 - layGroup.rotation.toDouble()
//                            )
//                        )).toInt()
//                        k = i * 2 + basew
//                        val m = j * 2 + baseh
//                        if (k > 150) {
//                            layoutParams.width = k
//                            layoutParams.leftMargin = margl - i
//                        }
//                        if (m > 150) {
//                            layoutParams.height = m
//                            layoutParams.topMargin = margt - j
//                        }
//                        layGroup.layoutParams = layoutParams
//                        layGroup.performLongClick()
//                    }
//                }
//                return@OnTouchListener true
//            }
//            freeze
//        })
//        btnrot.setOnTouchListener(OnTouchListener { paramAnonymousView, event ->
//            if (!freeze) {
//                layoutParams =
//                    layGroup.layoutParams as LayoutParams
//                layBg = this@ClipArt.parent as RelativeLayout
//                val arrayOfInt = IntArray(2)
//                layBg!!.getLocationOnScreen(arrayOfInt)
//                var i = event.rawX.toInt() - arrayOfInt[0]
//                var j = event.rawY.toInt() - arrayOfInt[1]
//                when (event.action) {
//                    MotionEvent.ACTION_DOWN -> {
//                        layGroup.invalidate()
//                        startDegree = layGroup.rotation
//                        pivx =
//                            layoutParams.leftMargin + this@ClipArt.width / 2
//                        pivy =
//                            layoutParams.topMargin + this@ClipArt.height / 2
//                        basex = i - pivx
//                        basey = pivy - j
//                    }
//                    MotionEvent.ACTION_MOVE -> {
//                        val k = pivx
//                        val m = pivy
//                        j = (Math.toDegrees(
//                            Math.atan2(
//                                basey.toDouble(),
//                                basex.toDouble()
//                            )
//                        )
//                                - Math.toDegrees(
//                            Math.atan2(
//                                m - j.toDouble(),
//                                i - k.toDouble()
//                            )
//                        )).toInt()
//                        i = j
//                        if (j < 0) {
//                            i = j + 360
//                        }
//                        layGroup.rotation = (startDegree + i) % 360.0f
//                    }
//                }
//                return@OnTouchListener true
//            }
//            freeze
//        })
//        btndel.setOnClickListener {
//            if (!freeze) {
//                layBg = this@ClipArt.parent as RelativeLayout
//                layBg!!.performClick()
//                layBg!!.removeView(layGroup)
//            }
//        }
//    }
}