package com.yhwang.nicole.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.yhwang.nicole.R
import com.yhwang.nicole.utilities.InjectorUtils
import com.yhwang.nicole.viewModel.ItemListViewModel


class ItemListFragment : Fragment() {

    companion object {
        fun newInstance() = ItemListFragment()
    }

    private val viewModel: ItemListViewModel by viewModels {
        InjectorUtils.provideItemListViewModeFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.itemDrawableList.observe(viewLifecycleOwner) { itemDrawableList ->
            val itemListRecyclerView = view.findViewById<RecyclerView>(R.id.item_list_RecyclerView)
            val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
            itemListRecyclerView.layoutManager = layoutManager
            itemListRecyclerView.adapter = ItemRecyclerViewAdapter(requireContext(), itemDrawableList)
        }
    }

    class ItemRecyclerViewAdapter(
        private val context: Context,
        private val itemBitmapList: ArrayList<Drawable>
    ) : RecyclerView.Adapter<ItemRecyclerViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
           return ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.card_view_item, parent, false))
        }

        override fun getItemCount(): Int = itemBitmapList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.imageView.setImageDrawable(itemBitmapList[position])
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.item_ImageView)
        }
    }
}