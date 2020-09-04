package com.yhwang.nicole.view

import android.annotation.SuppressLint
import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yhwang.nicole.R
import com.yhwang.nicole.model.Object2D
import com.yhwang.nicole.utility.fileToBitmap
import com.yhwang.nicole.utility.highlight
import timber.log.Timber
import java.util.*


@SuppressLint("ClickableViewAccessibility")
class ObjectViewFragment : Fragment() {

    private lateinit var object2D: Object2D
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            object2D = it["object2D"] as Object2D
        }
    }

    private lateinit var objectBgImageView: ImageView
    private lateinit var objectImageView: ImageView
    private lateinit var objectBitmap: Bitmap
    private var isOutlineDrawn = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_object_view, container, false)

        objectBgImageView = view.findViewById(R.id.object_bg_ImageView)
        objectImageView = view.findViewById(R.id.object_ImageView)

        return view
    }

    val timer = Timer()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.back_arrow_ImageView).setOnClickListener {
            findNavController().popBackStack()
        }

        objectBgImageView.setImageBitmap(fileToBitmap(requireContext(), object2D.backgroundFileName))
        objectBgImageView.clipToOutline = true

        objectBitmap = fileToBitmap(requireContext(), object2D.objectFileName)
        objectImageView.setImageBitmap(objectBitmap)
        objectImageView.clipToOutline = true
        objectImageView.setOnClickListener {
            Timber.i("navigate to ObjectViewFragment")
            val action = ObjectViewFragmentDirections
                .actionObjectViewFragmentToObjectDetailFragment(object2D)
            findNavController().navigate(action)
        }

        view.findViewById<ImageView>(R.id.view_in_ar_ImageView).setOnClickListener {
            val destination = ObjectViewFragmentDirections
                .actionObjectViewFragmentToObject2DCameraFragment(object2D)
            findNavController().navigate(destination)
        }

        timer.schedule(object : TimerTask() {
            override fun run() {
                requireActivity().runOnUiThread {
                    highlight()
                }
            }
        }, 0, 1000)
    }

    override fun onPause() {
        super.onPause()

        timer.cancel()
    }

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