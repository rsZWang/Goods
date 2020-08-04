package com.yhwang.nicole.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.os.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.easystudio.rotateimageview.RotateZoomImageView
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import com.yhwang.nicole.BuildConfig
import com.yhwang.nicole.R
import com.yhwang.nicole.utilities.InjectorUtils
import com.yhwang.nicole.viewModel.Camera2DViewModel
import kotlinx.android.synthetic.main.fragment_camera_2d.*
import timber.log.Timber


@SuppressLint("ClickableViewAccessibility")
class Camera2DFragment : Fragment() {

    var currentMode = Mode.RemoveBg
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_camera_2d, container, false)

        cameraView = view.findViewById(R.id.camera_View)
        cameraView.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                result.toBitmap { bitmap ->
                    if (bitmap != null) {
                        when (currentMode) {
                            Mode.RemoveBg -> {
                                Toast.makeText(requireContext(), "去背中...", Toast.LENGTH_LONG).show()
                                viewModel.getNoBgBitMap(bitmap)
                            }
                            Mode.ScreenShot -> {
                                viewModel.saveScreenShot(createScreenShotBitmap(draggable_layer_RelativeLayout, bitmap)) {
                                    requireActivity().runOnUiThread {
                                        take_Button.text = "去背"
                                        draggable_layer_RelativeLayout.removeAllViews()
                                        draggable_layer_RelativeLayout.background = null
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })

        view.findViewById<Button>(R.id.take_Button).setOnClickListener {
            cameraView.takePictureSnapshot()
        }

        return view
    }

    private lateinit var cameraView: CameraView
    private val viewModel: Camera2DViewModel by viewModels {
        InjectorUtils.provideCamera2DViewModeFactory(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        rotateZoomImageView.setOnTouchListener { view, motionEvent ->
//            rotateZoomImageView.onTouch(view, motionEvent)
//        }
//        if (Build.VERSION.SDK_INT >= 24){
//            View.DragShadowBuilder(rotateZoomImageView)
//        }
        cameraView.setLifecycleOwner(viewLifecycleOwner)
        viewModel.noBgBitmap.observe(viewLifecycleOwner) { bitmap ->
            Toast.makeText(requireContext(), "去背完成", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                Toast.makeText(requireContext(), "按下截圖並儲存至\"RemoveBg\"相簿", Toast.LENGTH_LONG).show()
            }, 2000)
            take_Button.text = "截圖"
            val rotateZoomImageView = RotateZoomImageView(requireContext())
            rotateZoomImageView.setImageBitmap(bitmap)
            rotateZoomImageView.setOnTouchListener { view, motionEvent -> rotateZoomImageView.onTouch(view, motionEvent) }
            if (Build.VERSION.SDK_INT >= 24){
                rotateZoomImageView.updateDragShadow(View.DragShadowBuilder(rotateZoomImageView))
            }
            val width = draggable_layer_RelativeLayout.width/3*2
            val height = draggable_layer_RelativeLayout.height/3*2
            val x = (draggable_layer_RelativeLayout.width - width)/2
            val y = (draggable_layer_RelativeLayout.height - height)/2
            val layoutParams = RelativeLayout.LayoutParams(width, height)
            layoutParams.marginStart = x
            layoutParams.topMargin = y
            draggable_layer_RelativeLayout.addView(rotateZoomImageView, layoutParams)
            currentMode = Mode.ScreenShot
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 0)
        }

        if (checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    enum class Mode {
        RemoveBg,
        ScreenShot
    }

    private fun createScreenShotBitmap(view: View, bitmap: Bitmap) : Bitmap {
        view.background = BitmapDrawable(resources, bitmap)
        Toast.makeText(requireContext(), "儲存截圖至相簿後將重置畫面", Toast.LENGTH_LONG).show()
        val screenBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(screenBitmap)
        view.draw(canvas)
        return screenBitmap
    }
}