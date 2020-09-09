package com.yhwang.nicole.view


import android.graphics.Color.WHITE
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
import com.yhwang.nicole.utility.assetsImageToBitmap
import com.yhwang.nicole.utility.checkArCompatibility
import com.yhwang.nicole.utility.fileToBitmap
import com.yhwang.nicole.utility.trimTransparentPart
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
                Timber.i("Object 2d id: %d", object2D.id)
            } else if (it["object3D"]!=null) {
                object3D = it["object3D"] as String
                mode = Mode.OBJECT_3D
                Timber.i("Object 3d id: %s", object3D)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_object_detail, container, false)
    }

    private lateinit var object3DSceneView: SceneView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.back_arrow_ImageView).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<ConstraintLayout>(R.id.view_in_ar_ConstraintLayout).setOnClickListener {
            Timber.i("View in AR")

            when (mode) {
                Mode.OBJECT_2D -> findNavController().navigate(
                    ObjectDetailFragmentDirections.actionObjectDetailFragmentToObject2DCameraFragment(
                        object2D
                    )
                )
                Mode.OBJECT_3D -> {
                    checkArCompatibility(requireActivity()) { isSupport ->
                        if (isSupport) {
                            findNavController().navigate(
                                ObjectDetailFragmentDirections.actionObjectDetailFragmentToObject3DCameraFragment(
                                    object3D
                                )
                            )
                        } else {
                            MaterialAlertDialogBuilder(requireContext())
                                .setMessage("此裝置不支援AR")
                                .setPositiveButton("OK", null)
                                .setCancelable(false)
                                .show()
                        }
                    }
                }
            }
        }

        when (mode) {
            Mode.OBJECT_2D -> {
                view.findViewById<ImageView>(R.id.object_2d_ImageView).setImageBitmap(
                    trimTransparentPart(fileToBitmap(requireContext(), object2D.objectFileName))
                )
                view.findViewById<ImageView>(R.id.object_2d_ImageView).visibility = View.VISIBLE
            }

            Mode.OBJECT_3D -> {
                checkArCompatibility(requireActivity()) { isSupport ->
                    if (isSupport) {
                        object3DSceneView = view.findViewById(R.id.object_3d_SceneView)
                        object3DSceneView.renderer!!.setClearColor(
                            com.google.ar.sceneform.rendering.Color(
                                WHITE
                            )
                        )
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
                        view.findViewById<ImageView>(R.id.object_2d_ImageView).setImageBitmap(
                            trimTransparentPart(
                                assetsImageToBitmap(
                                    requireContext().assets,
                                    "${object3D}.png"
                                )
                            )
                        )
                        view.findViewById<ImageView>(R.id.object_2d_ImageView).visibility =
                            View.VISIBLE
                    }
                }
            }
        }
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