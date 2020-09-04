package com.yhwang.nicole.view


import android.graphics.Color.WHITE
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer
import com.google.ar.sceneform.ux.TransformationSystem
import com.nouman.sceneview.nodes.DragTransformableNode
import com.yhwang.nicole.R
import com.yhwang.nicole.model.Object2D
import timber.log.Timber


class ObjectDetailFragment : Fragment() {

    private lateinit var obj: Object2D
    private lateinit var object3DSceneView: SceneView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_object_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.back_arrow_ImageView).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<ConstraintLayout>(R.id.view_in_ar_ConstraintLayout).setOnClickListener {
            Timber.i("View in AR")
            val destination = ObjectDetailFragmentDirections
                .actionObjectDetailFragmentToObject2DCameraFragment(obj)
            findNavController().navigate(destination)
        }

        arguments?.let {
            obj = it["Object"] as Object2D
            Timber.i("Object@d id: %d", obj.id)
        }
//        view.findViewById<ImageView>(R.id.item_ImageView).setImageBitmap(
//            trimTransparentPart(
//                fileToBitmap(
//                    requireContext(),
//                    item.itemFileName
//                )
//            )
//        )


        val transformationSystem = TransformationSystem(resources.displayMetrics, FootprintSelectionVisualizer())
        object3DSceneView = view.findViewById(R.id.object_3d_SceneView)
        object3DSceneView.renderer!!.setClearColor(com.google.ar.sceneform.rendering.Color(WHITE))
        object3DSceneView.resume()

        var localModel = "model.sfb"
        ModelRenderable.builder()
            .setSource(requireContext(),  Uri.parse(localModel))
            .build()
            .thenAccept { renderable: ModelRenderable ->
                addNodeToScene(renderable)
            }
            .exceptionally { throwable: Throwable? ->
                Timber.e("Unable to load Renderable. %s", throwable.toString())
                null
            }
    }

    private fun addNodeToScene(model: ModelRenderable) {
        if (object3DSceneView.scene != null) {
            val transformationSystem = TransformationSystem(resources.displayMetrics, FootprintSelectionVisualizer())
            DragTransformableNode(1f, transformationSystem).apply {
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