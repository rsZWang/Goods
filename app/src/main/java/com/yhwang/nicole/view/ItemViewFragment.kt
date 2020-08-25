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

        itemBgImageView = view.findViewById(R.id.item_bg_ImageView)
        itemImageView = view.findViewById(R.id.item_ImageView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.back_arrow_ImageView).setOnClickListener {
            findNavController().popBackStack()
        }

        itemBgImageView.setImageBitmap(fileToBitmap(requireContext(), item.backgroundFileName))
        itemBgImageView.clipToOutline = true

        itemBitmap = fileToBitmap(requireContext(), item.itemFileName)
        itemImageView.setImageBitmap(itemBitmap)
        itemImageView.clipToOutline = true
        itemImageView.setOnClickListener {
            drawOutline()
        }
        itemImageView.setOnLongClickListener {
            Timber.i("navigate to ItemViewFragment")
            val action = ItemViewFragmentDirections
                .actionItemViewFragmentToItemFragment(item)
            findNavController().navigate(action)
            false
        }

        view.findViewById<ImageView>(R.id.view_in_ar_ImageView).setOnClickListener {
            val destination = ItemViewFragmentDirections
                .actionItemViewFragmentToCamera2DFragment(item)
            findNavController().navigate(destination)
        }
    }

    private var outlineItemBitmap: Bitmap? = null
    private fun drawOutline() {
        if (isOutlineDrawn) {
            Timber.i("Remove outline")
            itemImageView.setImageBitmap(itemBitmap)
        } else {
            Timber.i("Draw outline")
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