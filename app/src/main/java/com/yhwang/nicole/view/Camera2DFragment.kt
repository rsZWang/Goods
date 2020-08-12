package com.yhwang.nicole.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.graphics.translationMatrix
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.easystudio.rotateimageview.RotateZoomImageView
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import com.yhwang.nicole.BuildConfig
import com.yhwang.nicole.R
import com.yhwang.nicole.model.Item
import com.yhwang.nicole.utility.*
import com.yhwang.nicole.viewModel.Camera2DViewModel
import kotlinx.android.synthetic.main.fragment_camera_2d.*
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import java.util.*


@SuppressLint("ClickableViewAccessibility")
class Camera2DFragment : Fragment() {

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
                        when (mode) {
                            Mode.RemoveBg -> {
                                Timber.d("remove bg mode")
                                Toast.makeText(requireContext(), "去背中...", Toast.LENGTH_LONG).show()
                                removeBitmapBg(bitmap)
                            }
                            Mode.ScreenShot -> {
                                Thread {
                                    Timber.d("screen shot mode")
                                    viewModel.saveItemAndBg(
                                        layoutToBitmap(draggable_item_RelativeLayout),
                                        rotateZoomImageView.x,
                                        rotateZoomImageView.y,
                                        bitmap
                                    ) {
                                        requireActivity().runOnUiThread {
                                            Toast.makeText(requireContext(), "存擋完成", Toast.LENGTH_LONG).show()
                                            take_Button.text = "清除背景"
                                            share_button.text = "分享"
                                            mode = Mode.ClearBg
                                        }
                                    }
                                    requireActivity().runOnUiThread { draggable_item_RelativeLayout.background = BitmapDrawable(resources, bitmap) }
                                }.start()
                            }
                            else -> Timber.e("mode error")
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

    private lateinit var mode: Mode
    private lateinit var rotateZoomImageView: RotateZoomImageView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        take_Button.setOnClickListener {
            when (mode) {
                Mode.RemoveBg -> cameraView.takePictureSnapshot()
                Mode.ScreenShot -> cameraView.takePictureSnapshot()
                Mode.ClearBg -> {
                    draggable_item_RelativeLayout.background = null
                    take_Button.text = "拍照"
                    share_button.text = "清空"
                    mode = Mode.ScreenShot
                }
            }
        }

        share_button.setOnClickListener {
            when (mode) {
                Mode.RemoveBg -> Toast.makeText(
                    requireContext(),
                    "請先去背",
                    Toast.LENGTH_LONG
                ).show()

                Mode.ScreenShot -> {
                    draggable_item_RelativeLayout.removeAllViews()
                    draggable_item_RelativeLayout.background = null
                    take_Button.text = "去背"
                    mode = Mode.RemoveBg
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

        if (requireArguments()["item"]!=null) {
            val item = requireArguments()["item"] as Item
            Timber.i("item id: %d", item.id)
            val bitmap = fileToBitmap(requireContext(), item.itemFileName)
            rotateZoomImageView = RotateZoomImageView(requireContext())
            rotateZoomImageView.setImageBitmap(bitmap)
            rotateZoomImageView.setOnTouchListener { view, motionEvent -> rotateZoomImageView.onTouch(view, motionEvent) }
            val layoutParams = RelativeLayout.LayoutParams(bitmap.width, bitmap.height)
            rotateZoomImageView.layoutParams = layoutParams
            draggable_item_RelativeLayout.addView(rotateZoomImageView, layoutParams)
            draggable_item_RelativeLayout.background = BitmapDrawable(resources, fileToBitmap(requireContext(), item.backgroundFileName))
            take_Button.text = "清除背景"
            share_button.text = "分享"
            mode = Mode.ClearBg
        } else {
            mode = Mode.RemoveBg
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

    fun removeBitmapBg(bitmap: Bitmap) {
        viewModel.removeBg(bitmap) { noBgBitmap ->
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "去背完成", Toast.LENGTH_SHORT).show()

                bitmapToFile(requireContext(), trimTransparentPart(noBgBitmap), "test.png", Bitmap.CompressFormat.PNG)
                val fileBitmap = fileToBitmap(requireContext(), "test.png")
                rotateZoomImageView = RotateZoomImageView(requireContext())
                rotateZoomImageView.setImageBitmap(fileBitmap)
                rotateZoomImageView.setOnTouchListener { view, motionEvent -> rotateZoomImageView.onTouch(view, motionEvent) }
                if (BuildConfig.DEBUG) { rotateZoomImageView.setBackgroundResource(R.drawable.border) }
                val layoutParams = RelativeLayout.LayoutParams(draggable_item_RelativeLayout.width, draggable_item_RelativeLayout.height)
                draggable_item_RelativeLayout.addView(rotateZoomImageView, layoutParams)

//                rotateZoomImageView = RotateZoomImageView(requireContext())
//                rotateZoomImageView.setImageBitmap(noBgBitmap)
//                rotateZoomImageView.setOnTouchListener { view, motionEvent -> rotateZoomImageView.onTouch(view, motionEvent) }
//                if (BuildConfig.DEBUG) { rotateZoomImageView.setBackgroundResource(R.drawable.border) }
//                val layoutParams = RelativeLayout.LayoutParams(draggable_item_RelativeLayout.width, draggable_item_RelativeLayout.height)
//                draggable_item_RelativeLayout.addView(rotateZoomImageView, layoutParams)
//
//                take_Button.text = "存擋"
//                mode = Mode.ScreenShot
            }
//            val trimmedBitmap = trimTransparentPart(bitmap)
//            rotateZoomImageView = RotateZoomImageView(requireContext())
//            rotateZoomImageView.setImageBitmap(trimmedBitmap)
//            rotateZoomImageView.setBackgroundResource(R.drawable.border)
//            rotateZoomImageView.setOnTouchListener { view, motionEvent -> rotateZoomImageView.onTouch(view, motionEvent) }
//            val width = trimmedBitmap.width/3*2
//            val height = trimmedBitmap.height/3*2
//            val layoutParams = RelativeLayout.LayoutParams(bitmap.width, bitmap.height)
//            layoutParams.marginStart = (draggable_item_RelativeLayout.width - width)/2
//            layoutParams.topMargin = (draggable_item_RelativeLayout.height - height)/2
//            draggable_item_RelativeLayout.addView(rotateZoomImageView, layoutParams)
        }
    }

    enum class Mode {
        RemoveBg,
        ScreenShot,
        ClearBg
    }
}