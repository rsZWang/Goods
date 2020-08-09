package com.yhwang.nicole.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.easystudio.rotateimageview.RotateZoomImageView
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import com.yhwang.nicole.R
import com.yhwang.nicole.utilities.InjectorUtils
import com.yhwang.nicole.utilities.layoutToBitmap
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

//        view.findViewById<Toolbar>(R.id.toolbar).setNavigationOnClickListener { findNavController().popBackStack() }

        cameraView = view.findViewById(R.id.camera_View)
        cameraView.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                result.toBitmap { bitmap ->
                    if (bitmap != null) {
                        when (currentMode) {
                            Mode.RemoveBg -> {
                                Timber.d("remove bg mode")
                                Toast.makeText(requireContext(), "去背中...", Toast.LENGTH_LONG).show()
                                viewModel.getNoBgBitMap(bitmap)
                            }
                            Mode.ScreenShot -> {
                                Thread {
                                    Timber.d("screen shot mode")
                                    viewModel.saveItemAndBg(layoutToBitmap(draggable_item_RelativeLayout), bitmap) {
                                        requireActivity().runOnUiThread {
                                            Toast.makeText(requireContext(), "存擋完成", Toast.LENGTH_LONG).show()
                                            take_Button.text = "清除背景"
                                            share_button.text = "分享"
                                            currentMode = Mode.ClearBg
                                        }
                                    }
                                    requireActivity().runOnUiThread { draggable_item_RelativeLayout.background = BitmapDrawable(resources, bitmap) }
                                }.start()
                            }
                        }
                    }
                }
            }
        })

        view.findViewById<Button>(R.id.back_Button).setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    private lateinit var cameraView: CameraView
    private val viewModel: Camera2DViewModel by viewModels {
        InjectorUtils.provideCamera2DViewModelFactory(requireContext())
    }

    private lateinit var rotateZoomImageView: RotateZoomImageView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        take_Button.setOnClickListener {
            when (currentMode) {
                Mode.RemoveBg -> cameraView.takePictureSnapshot()
                Mode.ScreenShot -> cameraView.takePictureSnapshot()
                Mode.ClearBg -> {
                    draggable_item_RelativeLayout.background = null
                    take_Button.text = "拍照"
                    share_button.text = "清空"
                    currentMode = Mode.ScreenShot
                }
            }
        }

        share_button.setOnClickListener {
            when (currentMode) {
                Mode.RemoveBg -> Toast.makeText(
                    requireContext(),
                    "請先去背",
                    Toast.LENGTH_LONG
                ).show()

                Mode.ScreenShot -> {
                    draggable_item_RelativeLayout.removeAllViews()
                    draggable_item_RelativeLayout.background = null
                    take_Button.text = "去背"
                    currentMode = Mode.RemoveBg
                }

                Mode.ClearBg -> viewModel.saveScreenToGallery(layoutToBitmap(draggable_item_RelativeLayout)) {
                    Toast.makeText(
                        requireContext(),
                        "已儲存至相簿",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        cameraView.setLifecycleOwner(viewLifecycleOwner)
        viewModel.noBgBitmap.observe(viewLifecycleOwner) { bitmap ->
            Toast.makeText(requireContext(), "去背完成", Toast.LENGTH_SHORT).show()
            rotateZoomImageView = RotateZoomImageView(requireContext())
            rotateZoomImageView.setImageBitmap(bitmap)
            rotateZoomImageView.setOnTouchListener { view, motionEvent -> rotateZoomImageView.onTouch(view, motionEvent) }
            if (Build.VERSION.SDK_INT >= 24){
                rotateZoomImageView.updateDragShadow(View.DragShadowBuilder(rotateZoomImageView))
            }
            val width = draggable_item_RelativeLayout.width/3*2
            val height = draggable_item_RelativeLayout.height/3*2
            val x = (draggable_item_RelativeLayout.width - width)/2
            val y = (draggable_item_RelativeLayout.height - height)/2
            val layoutParams = RelativeLayout.LayoutParams(width, height)
            layoutParams.marginStart = x
            layoutParams.topMargin = y
            draggable_item_RelativeLayout.addView(rotateZoomImageView, layoutParams)

            take_Button.text = "存擋"
            currentMode = Mode.ScreenShot
        }
    }

    override fun onResume() {
        super.onResume()
//        (activity as AppCompatActivity).toolbar.background =  ColorDrawable(Color.BLACK)

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
        ScreenShot,
        ClearBg
    }
}