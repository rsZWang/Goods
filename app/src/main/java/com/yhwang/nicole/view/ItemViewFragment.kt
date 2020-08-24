package com.yhwang.nicole.view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ddd.androidutils.DoubleClick
import com.ddd.androidutils.DoubleClickListener
import com.yhwang.nicole.R
import com.yhwang.nicole.model.Item
import com.yhwang.nicole.utility.drawOutline
import com.yhwang.nicole.utility.fileToBitmap
import timber.log.Timber


@SuppressLint("ClickableViewAccessibility")
class ItemViewFragment : Fragment() {

    private lateinit var item: Item
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            item = it["item"] as Item
        }
    }

    private lateinit var itemBgImageView: ImageView
    private lateinit var itemImageView: ImageView
    private lateinit var itemBitmap: Bitmap
    private var isOutlineDrawn = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_view, container, false)

        view.findViewById<ImageView>(R.id.back_arrow_ImageView).setOnClickListener {
            findNavController().popBackStack()
        }

        itemBgImageView = view.findViewById(R.id.item_bg_ImageView)
        itemBgImageView.setImageBitmap(fileToBitmap(requireContext(), item.backgroundFileName))
        itemBgImageView.clipToOutline = true

        itemImageView = view.findViewById(R.id.item_ImageView)
        itemBitmap = fileToBitmap(requireContext(), item.itemFileName)
        itemImageView.setImageBitmap(itemBitmap)
        itemImageView.clipToOutline = true
        itemImageView.setOnClickListener(DoubleClick(object : DoubleClickListener {
            override fun onSingleClickEvent(view: View?) {
                Timber.i("single click")
                drawOutline()
            }

            override fun onDoubleClickEvent(view: View?) {
                Timber.i("double click")
                val action = ItemViewFragmentDirections
                    .actionItemViewFragmentToItemFragment(item)
                findNavController().navigate(action)
            }
        }))

        view.findViewById<ImageView>(R.id.view_in_ar_ImageView).setOnClickListener {
            val destination = ItemViewFragmentDirections
                .actionItemViewFragmentToCamera2DFragment(item)
            findNavController().navigate(destination)
        }

        return view
    }

    private var outlineItemBitmap: Bitmap? = null
    private fun drawOutline() {
        if (isOutlineDrawn) {
            itemImageView.setImageBitmap(itemBitmap)
        } else {
            if (outlineItemBitmap == null) {
                outlineItemBitmap = drawOutline(
                    itemBitmap, ContextCompat.getColor(
                        requireContext(),
                        R.color.itemBlue
                    )
                )
            }
            itemImageView.setImageBitmap(outlineItemBitmap)
        }
        isOutlineDrawn = !isOutlineDrawn
    }
}