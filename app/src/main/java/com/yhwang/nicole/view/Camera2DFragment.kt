package com.yhwang.nicole.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.easystudio.rotateimageview.RotateZoomImageView
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import com.yhwang.nicole.R
import com.yhwang.nicole.model.Item
import com.yhwang.nicole.utilities.InjectorUtils
import com.yhwang.nicole.viewModel.Camera2DViewModel
import com.yhwang.nicole.viewModel.ItemListViewModel
import kotlinx.android.synthetic.main.fragment_camera_2d.*
import kotlinx.android.synthetic.main.fragment_item_list.*
import timber.log.Timber
import java.io.File


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
//                                saveScreenShot(draggable_layer_RelativeLayout, bitmap)
                                Thread {
                                    saveItemAndBg(rotateZoomImageView, bitmap)
                                }.start()
                            }
                        }
                    }
                }
            }
        })

        view.findViewById<Button>(R.id.take_Button).setOnClickListener {
            cameraView.takePictureSnapshot()
        }

//        view.findViewById<RelativeLayout>(R.id.draggable_layer_RelativeLayout).viewTreeObserver.addOnGlobalLayoutListener {
//            Timber.d("width: %d", relativeLayout.width)
//            Timber.d("height: %d", relativeLayout.height)
//        }

        return view
    }

    private lateinit var cameraView: CameraView
    private val viewModel: Camera2DViewModel by viewModels {
        InjectorUtils.provideCamera2DViewModelFactory(requireContext())
    }

    private val itemListViewModel: ItemListViewModel by viewModels {
        InjectorUtils.provideItemListViewModelFactory(requireContext())
    }

    lateinit var rotateZoomImageView: RotateZoomImageView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraView.setLifecycleOwner(viewLifecycleOwner)
        viewModel.noBgBitmap.observe(viewLifecycleOwner) { bitmap ->
            val borderBitmap = drawOutline(bitmap)
            Toast.makeText(requireContext(), "去背完成", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                Toast.makeText(requireContext(), "按下截圖並儲存至\"Goods\"相簿", Toast.LENGTH_LONG).show()
            }, 2000)
            take_Button.text = "截圖"
            rotateZoomImageView = RotateZoomImageView(requireContext())
            rotateZoomImageView.setImageBitmap(borderBitmap)
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

        Thread {
            itemListViewModel.itemList.observe(viewLifecycleOwner) {
                rotateZoomImageView = RotateZoomImageView(requireContext())
                rotateZoomImageView.setImageBitmap (loadJpgFileToBitmap(it[0].itemFileName))
                rotateZoomImageView.setOnTouchListener { view, motionEvent -> rotateZoomImageView.onTouch(view, motionEvent) }
                if (Build.VERSION.SDK_INT >= 24){
                    rotateZoomImageView.updateDragShadow(View.DragShadowBuilder(rotateZoomImageView))
                }
                val width = draggable_layer_RelativeLayout.width/3*2
                val height = draggable_layer_RelativeLayout.height/3*2
                val x = (draggable_layer_RelativeLayout.width - width)/2
                val y = (draggable_layer_RelativeLayout.height - height)/2
                val layoutParams = RelativeLayout.LayoutParams(width, height)
                layoutParams.topMargin = it[0].margin[0]
                layoutParams.bottomMargin = it[0].margin[1]
                layoutParams.marginStart = it[0].margin[2]
                layoutParams.bottomMargin = it[0].margin[3]
                draggable_layer_RelativeLayout.addView(rotateZoomImageView, layoutParams)

                currentMode = Mode.ScreenShot
            }
        }.start()
    }

    private fun loadJpgFileToBitmap(fileName: String) : Bitmap {
        // Initialize a new file instance to save bitmap object
        var file = ContextWrapper(context).getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE)
        file = File(file, "$fileName.jpg")
        return BitmapFactory.decodeFile(file.path)
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

    private fun saveScreenShot(view: View, bitmap: Bitmap) {
        Toast.makeText(requireContext(), "儲存截圖至相簿後將重置畫面", Toast.LENGTH_LONG).show()
        view.background = BitmapDrawable(resources, bitmap)
        val screenBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(screenBitmap)
        view.draw(canvas)
        Thread {
            viewModel.saveScreenShot(bitmap) {
                requireActivity().runOnUiThread {
                    take_Button.text = "去背"
                    draggable_layer_RelativeLayout.removeAllViews()
                    draggable_layer_RelativeLayout.background = null
                }
            }
        }.start()
    }

    private fun drawOutline(originalBitmap: Bitmap) : Bitmap {
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
            Color.WHITE,
            PorterDuff.Mode.SRC_ATOP
        ) //Color.WHITE is stroke color

        canvas.drawBitmap(originalBitmap, strokeWidth.toFloat(), strokeWidth.toFloat(), null)
        return newStrokedBitmap
    }

    private fun saveItemAndBg(itemView: View, background: Bitmap)  {
        Timber.d("save item")
        viewModel.saveItem(
            itemView,
            background
        ) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "item 新增完成", Toast.LENGTH_LONG).show()
            }
        }
    }

    enum class Mode {
        RemoveBg,
        ScreenShot
    }
}