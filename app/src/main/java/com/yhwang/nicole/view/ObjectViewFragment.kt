package com.yhwang.nicole.view

import android.annotation.SuppressLint
import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.ar.core.ArCoreApk
import com.yhwang.nicole.Mode
import com.yhwang.nicole.R
import com.yhwang.nicole.model.Object2D
import com.yhwang.nicole.utility.*
import timber.log.Timber
import java.util.*
import java.util.jar.Manifest


@SuppressLint("ClickableViewAccessibility")
class ObjectViewFragment : Fragment() {

    private lateinit var mode: Mode
    private lateinit var object2D: Object2D
    private lateinit var object3D: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it["object2D"]!=null) {
                object2D = it["object2D"] as Object2D
                mode = Mode.OBJECT_2D
                Timber.i("Object: ${object2D!!.objectFileName}")
                Timber.i("background: ${object2D!!.backgroundFileName}")
            } else if (it["object3D"]!=null) {
                object3D = it["object3D"] as String
                mode = Mode.OBJECT_3D
            }
        }
    }

    private var rootView: View? = null
    private lateinit var objectBgImageView: ImageView
    private lateinit var objectImageView: ImageView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_object_view, container, false)
            rootView!!.findViewById<ImageView>(R.id.back_arrow_ImageView).setOnClickListener {
                findNavController().popBackStack()
            }
            objectBgImageView = rootView!!.findViewById(R.id.object_bg_ImageView)
            objectImageView = rootView!!.findViewById(R.id.object_ImageView)

            if (mode == Mode.OBJECT_2D) {
                objectBgImageView.setImageBitmap(fileToBitmap(requireContext(), object2D, isBackground = true))
                objectBitmap = fileToBitmap(requireContext(), object2D, isBackground = false)
            } else if (mode == Mode.OBJECT_3D) {
                objectBgImageView.setImageBitmap(assetsImageToBitmap(requireContext().assets, "${object3D}_bg.jpeg"))
                objectBitmap = assetsImageToBitmap(requireContext().assets, "${object3D}.png")
            }
            objectBgImageView.clipToOutline = true
            objectImageView.setImageBitmap(objectBitmap)
            objectImageView.clipToOutline = true
            objectImageView.setOnClickListener {
                when (mode) {
                    Mode.OBJECT_2D -> {
                        findNavController().navigate(
                            ObjectViewFragmentDirections
                                .actionObjectViewFragmentToObjectDetailFragment(object2D, null)
                        )
                    }
                    Mode.OBJECT_3D -> {
                        checkArCompatibility(requireActivity()) { isSupport ->
                            if (isSupport) {
                                findNavController().navigate(ObjectViewFragmentDirections
                                    .actionObjectViewFragmentToObjectDetailFragment(null, object3D)
                                )
                            }  else {
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

            rootView!!.findViewById<ImageView>(R.id.view_in_ar_ImageView).setOnClickListener {
                checkPermission(requireActivity() as AppCompatActivity, android.Manifest.permission.CAMERA) {
                    if (it) {
                        when (mode) {
                            Mode.OBJECT_2D -> {
                                findNavController().navigate(
                                    ObjectViewFragmentDirections.actionObjectViewFragmentToObject2DCameraFragment(object2D)
                                )
                            }
                            Mode.OBJECT_3D -> {
                                checkArCoreCompatibility(requireActivity()) {
                                    findNavController().navigate(
                                        ObjectViewFragmentDirections.actionObjectViewFragmentToObject3DCameraFragment(object3D)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        return rootView
    }

    private lateinit var objectBitmap: Bitmap
    private lateinit var timer: Timer
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        timer = Timer()
//        timer.schedule(object : TimerTask() {
//            override fun run() {
//                requireActivity().runOnUiThread {
//                    highlight()
//                }
//            }
//        }, 0, 1000)
    }

    override fun onStop() {
        super.onStop()
//        timer.cancel()
    }

    private var isOutlineDrawn = false
    private var outlineObjectBitmap: Bitmap? = null
    private fun highlight() {
        if (isOutlineDrawn) {
            Timber.i("Remove outline")
            objectImageView.setImageBitmap(objectBitmap)
        } else {
            Timber.i("Draw outline")
            if (outlineObjectBitmap == null) {
                outlineObjectBitmap = highlight(requireContext(), objectBitmap)
            }
            objectImageView.setImageBitmap(outlineObjectBitmap)
        }
        isOutlineDrawn = !isOutlineDrawn
    }
}