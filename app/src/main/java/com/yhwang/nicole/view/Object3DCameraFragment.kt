package com.yhwang.nicole.view

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.ar.core.Config
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.yhwang.nicole.R
import com.yhwang.nicole.database.GoodsDatabase
import com.yhwang.nicole.repository.Object2DCameraRepository
import com.yhwang.nicole.repository.Object3DCameraRepository
import com.yhwang.nicole.utility.saveBitmapToGallery
import com.yhwang.nicole.viewModel.Object2DCameraViewModel
import com.yhwang.nicole.viewModel.Object3DCameraViewModel
import kotlinx.android.synthetic.main.fragment_object_3d_camera.take_Button
import timber.log.Timber
import java.lang.Exception
import java.util.*


class Object3DCameraFragment : Fragment() {

    enum class Mode {
        TAKE, RESET
    }

    private lateinit var object3D: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            object3D = it["object3D"] as String
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_object_3d_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val takeButton = view.findViewById<ImageButton>(R.id.take_Button)
        takeButton.setOnClickListener {
            takePhoto { result ->
                saveBitmapToGallery(requireContext(), result) {
                    Toast.makeText(
                        requireContext(), "Save successfully",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private lateinit var anchorNode: AnchorNode
    private lateinit var arFragment: ArFragment
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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
                .thenAccept { renderable: ModelRenderable ->
                    // Create the Anchor.
                    anchorNode = AnchorNode(hitResult.createAnchor())
                    anchorNode.setParent(arFragment.arSceneView.scene)

                    // Create the transformable model and add it to the anchor.
                    val modelNode = TransformableNode(arFragment.transformationSystem)
                    modelNode.setParent(anchorNode)
                    modelNode.scaleController.minScale = 0.01f
//                    modelNode.scaleController.maxScale = 0.5f
                    modelNode.localScale = Vector3(0.05f, 0.05f, 0.05f)
                    modelNode.renderable = renderable
                    modelNode.select()
                }
                .exceptionally {
                    Toast.makeText(
                        requireContext(),
                        "Unable to load andy renderable",
                        Toast.LENGTH_LONG
                    ).show()
                    null
                }
        }
    }

    private val viewModel: Object3DCameraViewModel by viewModels {
        Object3DCameraViewModel.Companion.Factory(
            Object3DCameraRepository(requireContext())
        )
    }

    private val object2DCameraViewModel: Object2DCameraViewModel by viewModels {
        Object2DCameraViewModel.Companion.Factory(
            Object2DCameraRepository(requireContext(), GoodsDatabase.getInstance(requireContext())!!)
        )
    }

    private fun takePhoto(callback: (Bitmap) -> Unit) {
        val view: ArSceneView = arFragment.arSceneView

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