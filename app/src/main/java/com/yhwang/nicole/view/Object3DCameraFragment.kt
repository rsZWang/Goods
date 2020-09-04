package com.yhwang.nicole.view

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.ar.core.Config
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.yhwang.nicole.R
import com.yhwang.nicole.utility.saveBitmapToGallery
import kotlinx.android.synthetic.main.fragment_object_3d_camera.*
import timber.log.Timber
import java.lang.Exception


class Object3DCameraFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
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

        take_Button.setOnClickListener {
            takePhoto()
        }
    }

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

        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
            ModelRenderable.builder()
                .setSource(requireContext(), R.raw.table)
                .build()
                .thenAccept { renderable: ModelRenderable ->
                    // Create the Anchor.
                    val anchor = hitResult.createAnchor()
                    val anchorNode = AnchorNode(anchor)
                    anchorNode.setParent(arFragment.arSceneView.scene)

                    // Create the transformable model and add it to the anchor.
                    val modelNode = TransformableNode(arFragment.transformationSystem)
                    modelNode.setParent(anchorNode)
                    modelNode.scaleController.minScale = 0.01f
                    modelNode.localScale = Vector3(0.05f, 0.05f, 0.05f)
//                    modelNode.scaleController.maxScale = 0.5f
                    modelNode.renderable = renderable
                    modelNode.select()
                }
                .exceptionally { throwable: Throwable? ->
                    Toast.makeText(
                        requireContext(),
                        "Unable to load andy renderable",
                        Toast.LENGTH_LONG
                    ).show()
                    null
                }
        }
    }

    private fun takePhoto() {
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
                    saveBitmapToGallery(requireContext(), bitmap) {
                        Toast.makeText(
                            requireContext(), "Save successfully",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(), "Save error",
                        Toast.LENGTH_LONG
                    ).show()
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