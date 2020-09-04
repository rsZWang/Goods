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
import android.view.ViewTreeObserver
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.easystudio.rotateimageview.RotateZoomImageView
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import com.yhwang.nicole.R
import com.yhwang.nicole.database.GoodsDatabase
import com.yhwang.nicole.model.Object2D
import com.yhwang.nicole.repository.Object2DCameraRepository
import com.yhwang.nicole.utility.*
import com.yhwang.nicole.viewModel.Object2DCameraViewModel
import kotlinx.android.synthetic.main.fragment_object_2d_camera.*
import timber.log.Timber


@SuppressLint("ClickableViewAccessibility")
class Object2DCameraFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_object_2d_camera, container, false)

        view.findViewById<ImageButton>(R.id.back_Button).setOnClickListener {
            findNavController().popBackStack()
        }

        cameraView = view.findViewById(R.id.camera_View)
        objectContainerRelativeLayout = view.findViewById(R.id.object_container_RelativeLayout)

        return view
    }

    private lateinit var cameraView: CameraView
    private val viewModel: Object2DCameraViewModel by viewModels {
        Object2DCameraViewModel.Companion.Factory(
            Object2DCameraRepository(requireContext(), GoodsDatabase.getInstance(requireContext())!!))
    }

    private lateinit var mode: Mode
    private lateinit var rotateZoomImageView: RotateZoomImageView
    private lateinit var objectContainerRelativeLayout: RelativeLayout
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        take_Button.setOnClickListener {
            when (mode) {
                Mode.REMOVE_BG -> cameraView.takePictureSnapshot()
                Mode.SCREEN_SHOT -> cameraView.takePictureSnapshot()
                Mode.SHARE -> {
                    objectContainerRelativeLayout.background = null
                    take_Button.setImageResource(R.mipmap.button_camera)
                    mode = Mode.SCREEN_SHOT
                }
            }
        }

        share_button.setOnClickListener {
            when (mode) {
                Mode.REMOVE_BG -> Toast.makeText(
                    requireContext(),
                    "請先去背",
                    Toast.LENGTH_LONG
                ).show()

                Mode.SCREEN_SHOT -> Toast.makeText(
                    requireContext(),
                    "請先拍照",
                    Toast.LENGTH_LONG
                ).show()

                Mode.SHARE -> viewModel.saveScreenToGallery(layoutToBitmap(objectContainerRelativeLayout)) {
                    Toast.makeText(
                        requireContext(),
                        "已儲存至相簿",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        cameraView.setLifecycleOwner(viewLifecycleOwner)
        cameraView.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                result.toBitmap { bitmap ->
                    if (bitmap != null) {
                        when (mode) {
                            Mode.REMOVE_BG -> {
                                Timber.d("remove bg mode")
                                Toast.makeText(requireContext(), "去背中...", Toast.LENGTH_LONG).show()
                                removeBitmapBg(bitmap)
                            }
                            Mode.SCREEN_SHOT -> {
                                Thread {
                                    Timber.d("screen shot mode")
                                    viewModel.saveObjectAndBg(
                                        layoutToBitmap(objectContainerRelativeLayout),
                                        rotateZoomImageView.x,
                                        rotateZoomImageView.y,
                                        bitmap
                                    ) {
                                        requireActivity().runOnUiThread {
                                            Toast.makeText(requireContext(), "存擋完成", Toast.LENGTH_LONG).show()
                                            take_Button.setImageResource(R.mipmap.button_reset)
                                            mode = Mode.SHARE
                                        }
                                    }
                                    requireActivity().runOnUiThread { objectContainerRelativeLayout.background = BitmapDrawable(resources, bitmap) }
                                }.start()
                            }
                            else -> Timber.e("mode error")
                        }
                    }
                }
            }
        })

        if (requireArguments()["object"]!=null) {
            val object2D = requireArguments()["object"] as Object2D
            Timber.i("Object2D id: %d", object2D.id)
            val bitmap = trimTransparentPart(fileToBitmap(requireContext(), object2D.objectFileName))
            rotateZoomImageView = RotateZoomImageView(requireContext())
            rotateZoomImageView.setImageBitmap(bitmap)
            rotateZoomImageView.setOnTouchListener { view, motionEvent -> rotateZoomImageView.onTouch(view, motionEvent) }
            val layoutParams = RelativeLayout.LayoutParams(bitmap.width, bitmap.height)
            objectContainerRelativeLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    objectContainerRelativeLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    rotateZoomImageView.x = ((objectContainerRelativeLayout.width - bitmap.width)/2).toFloat()
                    rotateZoomImageView.y = ((objectContainerRelativeLayout.height - bitmap.height)/2).toFloat()
                    objectContainerRelativeLayout.addView(rotateZoomImageView, layoutParams)
                }
            })
            take_Button.setImageResource(R.mipmap.button_camera)
            mode = Mode.SCREEN_SHOT
        } else {
            mode = Mode.REMOVE_BG
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

    fun removeBitmapBg(bitmap: Bitmap) {
        viewModel.removeBg(bitmap) { noBgBitmap ->
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "去背完成", Toast.LENGTH_SHORT).show()

                rotateZoomImageView = RotateZoomImageView(requireContext())
                rotateZoomImageView.setImageBitmap(noBgBitmap)
                rotateZoomImageView.setOnTouchListener { view, motionEvent -> rotateZoomImageView.onTouch(view, motionEvent) }
//                if (BuildConfig.DEBUG) { rotateZoomImageView.setBackgroundResource(R.drawable.border) }
                val layoutParams = RelativeLayout.LayoutParams(objectContainerRelativeLayout.width, objectContainerRelativeLayout.height)
                objectContainerRelativeLayout.addView(rotateZoomImageView, layoutParams)

                take_Button.setImageResource(R.mipmap.button_camera)
                mode = Mode.SCREEN_SHOT
            }
        }
    }

    enum class Mode {
        REMOVE_BG,
        SCREEN_SHOT,
        SHARE
    }
}