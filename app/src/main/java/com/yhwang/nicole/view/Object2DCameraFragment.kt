package com.yhwang.nicole.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.easystudio.rotateimageview.RotateZoomImageView
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Facing
import com.yhwang.nicole.R
import com.yhwang.nicole.database.GoodsDatabase
import com.yhwang.nicole.model.Object2D
import com.yhwang.nicole.repository.Object2DCameraRepository
import com.yhwang.nicole.utility.*
import com.yhwang.nicole.viewModel.Object2DCameraViewModel
import com.yhwang.nicole.viewModel.Object2DCameraViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.concurrent.thread

@SuppressLint("ClickableViewAccessibility")
class Object2DCameraFragment : Fragment() {

    enum class Mode {
        REMOVE_BG,
        SCREEN_SHOT,
        SHARE
    }

    private lateinit var storagePermissionCheckerLauncher: ActivityResultLauncher<String>
    private var storagePermissionCheckerCallback: ((Boolean) -> Unit)? = null
    private lateinit var cameraPermissionCheckerLauncher: ActivityResultLauncher<String>
    private var cameraPermissionCheckerCallback: ((Boolean) -> Unit)? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        storagePermissionCheckerLauncher = registerPermissionCheckLauncher(
            requireActivity(),
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) { isGranted ->
            storagePermissionCheckerCallback?.invoke(isGranted)
        }
        cameraPermissionCheckerLauncher = registerPermissionCheckLauncher(
            requireActivity(),
            this,
            Manifest.permission.CAMERA
        ) { isGranted ->
            cameraPermissionCheckerCallback?.invoke(isGranted)
        }
    }

    private fun checkStoragePermission(callback: (Boolean) -> Unit ) {
        storagePermissionCheckerCallback = callback
        storagePermissionCheckerLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun checkCameraPermission(callback: (Boolean) -> Unit ) {
        cameraPermissionCheckerCallback = callback
        cameraPermissionCheckerLauncher.launch(Manifest.permission.CAMERA)
    }

    private lateinit var flashAnimationView: View
    private lateinit var takeButton: ImageButton
    private lateinit var shareButton: ImageButton
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_object_2d_camera, container, false)

        takeButton = view.findViewById(R.id.take_Button)
        view.findViewById<ImageButton>(R.id.back_Button).setOnClickListener {
            findNavController().popBackStack()
        }
        shareButton = view.findViewById(R.id.share_button)
        shareButton.visibility = View.GONE

        cameraView = view.findViewById(R.id.camera_View)
        cameraView.setLifecycleOwner(viewLifecycleOwner)
        objectContainerRelativeLayout = view.findViewById(R.id.object_container_RelativeLayout)
        flashAnimationView = view.findViewById(R.id.flash_animation_view)

        return view
    }

    private lateinit var cameraView: CameraView
    private val viewModel: Object2DCameraViewModel by viewModels {
        Object2DCameraViewModelFactory(Object2DCameraRepository(requireContext(), GoodsDatabase.getInstance(requireContext())!!))
    }

    private lateinit var progressAlertDialog: AlertDialog
    private var imageUri: Uri? = null
    private lateinit var mode: Mode
    private lateinit var rotateZoomImageView: RotateZoomImageView
    private lateinit var objectContainerRelativeLayout: RelativeLayout
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        takeButton.setOnClickListener {
            if (mode != Mode.SHARE) {
                cameraView.takePictureSnapshot()
                GlobalScope.launch(Dispatchers.Main) {
                    flashAnimationView.visibility = View.VISIBLE
                    flashAnimationView.startAnimation(AlphaAnimation(1f, 0f).apply {
                        duration = 1000
                        setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(p0: Animation?) { }
                            override fun onAnimationEnd(p0: Animation?) {
                                flashAnimationView.visibility = View.GONE
                            }
                            override fun onAnimationRepeat(p0: Animation?) { }
                        })
                    })
                }
            } else {
                takeButton.setImageResource(R.mipmap.button_camera)
                shareButton.visibility = View.INVISIBLE
                objectContainerRelativeLayout.background = null
                mode = Mode.SCREEN_SHOT
            }
        }

        shareButton.setOnClickListener {
            share(requireActivity(), imageUri!!)
        }

        var currentFacing = Facing.BACK
        view.findViewById<ImageButton>(R.id.flip_camera_button).setOnClickListener {
            currentFacing  = when (currentFacing) {
                Facing.BACK -> Facing.FRONT
                Facing.FRONT -> Facing.BACK
            }
            cameraView.facing = currentFacing
        }

        cameraView.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                result.toBitmap { bitmap ->
                    if (bitmap != null) {
                        when (mode) {
                            Mode.REMOVE_BG -> {
                                Timber.i("remove bg mode")
                                GlobalScope.launch(Dispatchers.Main) {
                                    Toast.makeText(requireContext(), "去背中...", Toast.LENGTH_LONG).show()
                                    progressAlertDialog = showProgressDialog(requireContext())
                                }
                                removeBitmapBg(bitmap)
                            }
                            Mode.SCREEN_SHOT -> {
                                Timber.i("screen shot mode")
                                Toast.makeText(requireContext(), "儲存至APP中...", Toast.LENGTH_SHORT).show()
                                viewModel.saveObjectAndBg(
                                    layoutToBitmap(objectContainerRelativeLayout),
                                    rotateZoomImageView.x,
                                    rotateZoomImageView.y,
                                    bitmap
                                ) {
                                    mode = Mode.SHARE
                                    GlobalScope.launch(Dispatchers.Main) {
                                        Toast.makeText(requireContext(), "成功儲存至APP", Toast.LENGTH_SHORT).show()
                                        objectContainerRelativeLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                                            override fun onGlobalLayout() {
                                                objectContainerRelativeLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                                                Toast.makeText(requireContext(), "儲存至手機中...", Toast.LENGTH_SHORT).show()
                                                viewModel.saveScreenToGallery(layoutToBitmap(objectContainerRelativeLayout)) { uri ->
                                                    GlobalScope.launch(Dispatchers.Main) {
                                                        imageUri = uri
                                                        Toast.makeText(requireContext(), "成功儲存至相簿", Toast.LENGTH_SHORT).show()
                                                        takeButton.setImageResource(R.mipmap.button_reset)
                                                        shareButton.visibility = View.VISIBLE
                                                        progressAlertDialog.dismiss()
                                                    }
                                                }
                                            }
                                        })
                                        progressAlertDialog = showProgressDialog(requireContext())
                                        objectContainerRelativeLayout.background = BitmapDrawable(resources, bitmap)
                                    }
                                }
                            }
                            else -> Timber.e("mode error")
                        }
                    }
                }
            }
        })

        if (requireArguments()["object2D"]!=null) {
            val object2D = requireArguments()["object2D"] as Object2D
            Timber.i("Object2D id: ${object2D.id}")
            val bitmap = trimTransparentPart(fileToBitmap(requireContext(), object2D, isBackground = false))
            rotateZoomImageView = RotateZoomImageView(requireContext())
            rotateZoomImageView.adjustViewBounds = true
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
            takeButton.setImageResource(R.mipmap.button_camera)
            mode = Mode.SCREEN_SHOT
        } else {
            mode = Mode.REMOVE_BG
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            checkCameraPermission { }
        }
        if (checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            checkStoragePermission { }
        }
    }

    fun removeBitmapBg(bitmap: Bitmap) {
        viewModel.removeBg(bitmap) { noBgBitmap ->
            GlobalScope.launch(Dispatchers.Main) {
                rotateZoomImageView = RotateZoomImageView(requireContext())
                rotateZoomImageView.setImageBitmap(noBgBitmap)
                rotateZoomImageView.setOnTouchListener { view, motionEvent -> rotateZoomImageView.onTouch(view, motionEvent) }
//                if (BuildConfig.DEBUG) { rotateZoomImageView.setBackgroundResource(R.drawable.border) }
                val layoutParams = RelativeLayout.LayoutParams(objectContainerRelativeLayout.width, objectContainerRelativeLayout.height)
                objectContainerRelativeLayout.addView(rotateZoomImageView, layoutParams)

                takeButton.setImageResource(R.mipmap.button_camera)
                mode = Mode.SCREEN_SHOT

                Toast.makeText(requireContext(), "去背完成", Toast.LENGTH_SHORT).show()
                progressAlertDialog.dismiss()
            }
        }
    }
}