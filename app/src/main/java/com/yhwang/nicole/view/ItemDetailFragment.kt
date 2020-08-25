package com.yhwang.nicole.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import com.yhwang.nicole.R
import com.yhwang.nicole.model.Item
import com.yhwang.nicole.utility.fileToBitmap
import com.yhwang.nicole.utility.trimTransparentPart
import timber.log.Timber

class ItemDetailFragment : Fragment() {

    private lateinit var item: Item

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.back_arrow_ImageView).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<ConstraintLayout>(R.id.view_in_ar_ConstraintLayout).setOnClickListener {
            Timber.i("View in AR")
            val destination = ItemDetailFragmentDirections
                .actionItemDetailFragmentToCamera2DFragment(item)
            findNavController().navigate(destination)
        }

        arguments?.let {
            item = it["item"] as Item
            Timber.i("item id: %d", item.id)
        }
        view.findViewById<ImageView>(R.id. item_ImageView).setImageBitmap(trimTransparentPart(fileToBitmap(requireContext(), item.itemFileName)))
    }
}