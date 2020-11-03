package com.yhwang.nicole.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.yhwang.nicole.R
import com.yhwang.nicole.utility.checkPermission
import com.yhwang.nicole.utility.saveBitmapToGallery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception


class Object3DCameraFragment : Fragment() {

    private lateinit var object3D: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            object3D = it["object3D"] as String
        }
    }

    private lateinit var tookBitmap: Bitmap
    private lateinit var tookPicture: ImageView
    private lateinit var takeButton: ImageButton
    private lateinit var shareButton: ImageButton
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_object_3d_camera, container, false)

        tookPicture = view.findViewById(R.id.took_picture_imageView)
        tookPicture.visibility = View.GONE

        takeButton = view.findViewById(R.id.take_Button)
        takeButton.setOnClickListener {
            tookPicture.visibility = View.VISIBLE
            tookPicture.startAnimation(AlphaAnimation(1f, 0f).apply {
                duration = 1000
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(p0: Animation?) { }
                    override fun onAnimationEnd(p0: Animation?) {
                        takePhoto(arFragment.arSceneView) { result ->
                            tookBitmap = result
                            GlobalScope.launch(Dispatchers.Main) {
                                arFragment.arSceneView.pause()
                                arFragment.arSceneView.visibility = View.GONE
                                takeButton.visibility = View.GONE

                                tookPicture.setImageBitmap(result)
                                shareButton.visibility = View.VISIBLE
                            }
                        }
                    }
                    override fun onAnimationRepeat(p0: Animation?) { }
                })
            })
        }

        shareButton = view.findViewById(R.id.share_button)
        shareButton.visibility = View.GONE
        shareButton.setOnClickListener {
            saveBitmapToGallery(requireContext(), tookBitmap) {
                Toast.makeText(
                    requireContext(), "Save to gallery successfully",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        view.findViewById<ImageButton>(R.id.back_Button).setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startArSession()
    }

    private lateinit var anchorNode: AnchorNode
    private lateinit var arFragment: ArFragment
    private fun startArSession() {
        arFragment = childFragmentManager.findFragmentById(R.id.ar_3d_model_Fragment) as ArFragment
        val session = Session(requireContext())
        val config = Config(session)
        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
        session.configure(config)
        arFragment.arSceneView.setupSession(session)

        arFragment.setOnTapArPlaneListener { hitResult: HitResult, _: Plane?, _: MotionEvent? ->
            ModelRenderable.builder()
                .setSource(requireContext()) { requireContext().assets.open("object/$object3D.sfb") }
                .build()
                .thenAccept { model: ModelRenderable ->
                    // Create the Anchor.
                    anchorNode = AnchorNode(hitResult.createAnchor())
                    anchorNode.setParent(arFragment.arSceneView.scene)
//                    anchorNode.localScale = Vector3(1f, 1f, 1f)

                    // Create the transformable model and add it to the anchor.
                    val modelNode = TransformableNode(arFragment.transformationSystem)
                    modelNode.setParent(anchorNode)
                    modelNode.scaleController.minScale = 0.1f

                    val size =  (model.collisionShape as Box).size
                    val min = size.x.coerceAtMost(size.y.coerceAtMost(size.z))
                    Timber.i("model size: x=%f, y=%f, z=%f", size.x, size.y, size.z)
                    modelNode.scaleController.maxScale = 1/min
                    modelNode.renderable = model
                    modelNode.select()
                }.exceptionally {
                    Toast.makeText(
                        requireContext(),
                        "Unable to load andy renderable",
                        Toast.LENGTH_LONG
                    ).show()
                    null
                }
        }
    }

    private fun takePhoto(view: ArSceneView, callback: (Bitmap) -> Unit) {
        // Create a bitmap the size of the scene view.
        val bitmap = Bitmap.createBitmap(
            view.width, view.height,
            Bitmap.Config.ARGB_8888
        )

        // Create a handler thread to offload the processing of the image.
        val handlerThread = HandlerThread("PixelCopier")
        handlerThread.start()
        // Make the request to copy.
        PixelCopy.request(view, bitmap, { copyResult ->
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    Toast.makeText(
                        requireContext(), "Take successfully",
                        Toast.LENGTH_LONG
                    ).show()
                    callback(bitmap)
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(), "Take error",
                        Toast.LENGTH_LONG
                    ).show()
                    e.printStackTrace()
                    return@request
                }
            } else {
                val toast = Toast.makeText(
                    requireContext(),
                    "Failed to copyPixels: $copyResult", Toast.LENGTH_LONG
                )
                toast.show()
            }
            handlerThread.quitSafely()
        }, Handler(handlerThread.looper))
    }
}

//arFragment.arSceneView.planeRenderer.isVisible = false
//Timer().schedule(object : TimerTask() {
//    override fun run() {
//        takePhoto { originalBitmap ->
//            requireActivity().runOnUiThread {
//                arFragment.arSceneView.scene.removeChild(anchorNode)
//                Timer().schedule(object : TimerTask() {
//                    override fun run() {
//                        takePhoto { bgBitmap ->
//                            object2DCameraViewModel.removeBg(originalBitmap) { objectBitmap ->
//                                viewModel.saveObjectAndBg(
//                                    originalBitmap,
//                                    objectBitmap,
//                                    bgBitmap
//                                ) {
//                                    requireActivity().runOnUiThread {
//                                        anchorNode.setParent(arFragment.arSceneView.scene)
//                                        arFragment.arSceneView.planeRenderer.isVisible = true
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }, 100)
//            }
//        }
//    }
//}, 100)