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
import android.widget.Button
import android.widget.ImageButton
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

        view.findViewById<ImageButton>(R.id.back_Button).setOnClickListener {
            findNavController().popBackStack()
        }

        cameraView = view.findViewById(R.id.camera_View)
        itemContainerRelativeLayout = view.findViewById(R.id.item_container_RelativeLayout)

        return view
    }

    private lateinit var cameraView: CameraView
    private val viewModel: Camera2DViewModel by viewModels {
        InjectorUtils.provideCamera2DViewModelFactory(requireContext())
    }

    private lateinit var mode: Mode
    private lateinit var rotateZoomImageView: RotateZoomImageView
    private lateinit var itemContainerRelativeLayout: RelativeLayout
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        take_Button.setOnClickListener {
            when (mode) {
                Mode.REMOVE_BG -> cameraView.takePictureSnapshot()
                Mode.SCREEN_SHOT -> cameraView.takePictureSnapshot()
                Mode.SHARE -> {
                    itemContainerRelativeLayout.background = null
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

                Mode.SHARE -> viewModel.saveScreenToGallery(layoutToBitmap(itemContainerRelativeLayout)) {
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
                                    viewModel.saveItemAndBg(
                                        layoutToBitmap(itemContainerRelativeLayout),
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
                                    requireActivity().runOnUiThread { itemContainerRelativeLayout.background = BitmapDrawable(resources, bitmap) }
                                }.start()
                            }
                            else -> Timber.e("mode error")
                        }
                    }
                }
            }
        })

        if (requireArguments()["item"]!=null) {
            val item = requireArguments()["item"] as Item
            Timber.i("item id: %d", item.id)
            val bitmap = trimTransparentPart(fileToBitmap(requireContext(), item.itemFileName))
            rotateZoomImageView = RotateZoomImageView(requireContext())
            rotateZoomImageView.setImageBitmap(bitmap)
            rotateZoomImageView.setOnTouchListener { view, motionEvent -> rotateZoomImageView.onTouch(view, motionEvent) }
            val layoutParams = RelativeLayout.LayoutParams(bitmap.width, bitmap.height)
            itemContainerRelativeLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    itemContainerRelativeLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    rotateZoomImageView.x = ((itemContainerRelativeLayout.width - bitmap.width)/2).toFloat()
                    rotateZoomImageView.y = ((itemContainerRelativeLayout.height - bitmap.height)/2).toFloat()
                    itemContainerRelativeLayout.addView(rotateZoomImageView, layoutParams)
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
                val layoutParams = RelativeLayout.LayoutParams(itemContainerRelativeLayout.width, itemContainerRelativeLayout.height)
                itemContainerRelativeLayout.addView(rotateZoomImageView, layoutParams)

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