package com.yhwang.nicole.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer
import com.google.ar.sceneform.ux.TransformationSystem
import com.yhwang.nicole.Mode
import com.yhwang.nicole.R
import com.yhwang.nicole.model.Object2D
import com.yhwang.nicole.nodes.DragTransformableNode
import com.yhwang.nicole.utility.*
import timber.log.Timber

class ObjectDetailFragment : Fragment() {

    private lateinit var mode: Mode
    private lateinit var object2D: Object2D
    private lateinit var object3D: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it["object2D"]!=null) {
                object2D = it["object2D"] as Object2D
                mode = Mode.OBJECT_2D
                Timber.i("Object 2d id: ${object2D.id}")
            } else if (it["object3D"]!=null) {
                object3D = it["object3D"] as String
                mode = Mode.OBJECT_3D
                Timber.i("Object 3d id: $object3D")
            }
        }
    }

    private var rootView: View? = null
    private lateinit var object3DSceneView: SceneView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_object_detail, container, false)
            rootView!!.findViewById<ImageView>(R.id.back_arrow_ImageView).setOnClickListener {
                findNavController().popBackStack()
            }
            rootView!!.findViewById<ConstraintLayout>(R.id.view_in_ar_ConstraintLayout).setOnClickListener {
                Timber.i("View in AR")
                checkPermission(requireActivity() as AppCompatActivity, android.Manifest.permission.CAMERA) {
                    when (mode) {
                        Mode.OBJECT_2D -> findNavController().navigate(
                            ObjectDetailFragmentDirections.actionObjectDetailFragmentToObject2DCameraFragment(
                                object2D
                            )
                        )
                        Mode.OBJECT_3D -> {
                            checkArCoreCompatibility(requireActivity()) {
                                findNavController().navigate(
                                    ObjectDetailFragmentDirections.actionObjectDetailFragmentToObject3DCameraFragment(
                                        object3D
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        when (mode) {
            Mode.OBJECT_2D -> {
                rootView!!.findViewById<ImageView>(R.id.object_2d_ImageView).setImageBitmap(
                    trimTransparentPart(
                        fileToBitmap(requireContext(), object2D, isBackground = false)
                    )
                )
                rootView!!.findViewById<ImageView>(R.id.object_2d_ImageView).visibility = View.VISIBLE
            }

            Mode.OBJECT_3D -> {
                checkArCompatibility(requireActivity()) { isSupport ->
                    if (isSupport) {
                        object3DSceneView = rootView!!.findViewById(R.id.object_3d_SceneView)
                        object3DSceneView.visibility = View.VISIBLE
                        object3DSceneView.resume()
                        ModelRenderable.builder()
                            .setSource(requireContext()) { requireContext().assets.open("object/$object3D.sfb") }
                            .build()
                            .thenAccept { renderable: ModelRenderable ->
                                addNodeToScene(renderable)
                            }
                            .exceptionally { throwable: Throwable? ->
                                Timber.e("Unable to load Renderable. %s", throwable.toString())
                                null
                            }
                    } else {
                        rootView!!.findViewById<ImageView>(R.id.object_2d_ImageView).setImageBitmap(
                            trimTransparentPart(
                                assetsImageToBitmap(
                                    requireContext().assets,
                                    "${object3D}.png"
                                )
                            )
                        )
                        rootView!!.findViewById<ImageView>(R.id.object_2d_ImageView).visibility = View.VISIBLE
                    }
                }
            }
        }
        return rootView
    }

    private fun addNodeToScene(model: ModelRenderable) {
        if (object3DSceneView.scene != null) {
            val size =  (model.collisionShape as Box).size
            val radius = size.x.coerceAtLeast(size.y.coerceAtLeast(size.z))

            val transformationSystem = TransformationSystem(
                resources.displayMetrics,
                FootprintSelectionVisualizer()
            )
            DragTransformableNode(radius, transformationSystem).apply {
                renderable = model
                object3DSceneView.scene.addChild(this)
                select()
            }
            object3DSceneView.scene.addOnPeekTouchListener { hitTestResult: HitTestResult?, motionEvent: MotionEvent? ->
                transformationSystem.onTouch(
                    hitTestResult,
                    motionEvent
                )
            }
        }
    }
}