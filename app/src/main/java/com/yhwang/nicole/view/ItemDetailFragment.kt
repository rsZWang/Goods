package com.yhwang.nicole.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yhwang.nicole.R
import com.yhwang.nicole.model.Item
import com.yhwang.nicole.utility.InjectorUtils
import com.yhwang.nicole.utility.cropBitmapTransparency
import com.yhwang.nicole.utility.fileToBitmap
import com.yhwang.nicole.viewModel.ItemDetailViewModel
import kotlinx.android.synthetic.main.fragment_detail_item.*
import timber.log.Timber

class ItemDetailFragment : Fragment() {

    private lateinit var item: Item
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            item = it["item"] as Item
            Timber.i("item id: %d", item.id)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detail_item, container, false)

        view.findViewById<ImageView>(R.id.back_arrow_ImageView).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<ImageView>(R.id. item_ImageView).setImageBitmap(cropBitmapTransparency(fileToBitmap(requireContext(), item.itemFileName)))

        view.findViewById<ConstraintLayout>(R.id.view_in_ar_ConstraintLayout).setOnClickListener {
            view_in_ar_ConstraintLayout.setOnClickListener {
                Timber.i("View in AR")
                val destination = ItemDetailFragmentDirections
                    .actionItemDetailFragmentToCamera2DFragment(item)
                findNavController().navigate(destination)
            }
        }

        return view
    }
}