package com.yhwang.nicole.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yhwang.nicole.R
import com.yhwang.nicole.model.Item
import com.yhwang.nicole.utilities.*
import com.yhwang.nicole.viewModel.ItemListViewModel
import kotlinx.android.synthetic.main.fragment_item_list.*
import timber.log.Timber
import kotlin.collections.ArrayList


class ItemListFragment : Fragment() {

    companion object {
        fun newInstance() = ItemListFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)

        view.findViewById<ImageView>(R.id.to_camera_fragment_Button).setOnClickListener {
            Timber.i("to camera 2D fragment")
            val destination = ItemListFragmentDirections
                .actionItemListFragmentToCamera2DFragment()
            view.findNavController().navigate(destination)
        }

        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.isItemPrefetchEnabled = true
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        val itemRecyclerView = view.findViewById<RecyclerView>(R.id.item_list_RecyclerView)
        itemRecyclerView.layoutManager = layoutManager
        itemRecyclerView.adapter = ItemRecyclerViewAdapter(requireContext())

        return view
    }

    private val viewModel: ItemListViewModel by viewModels {
        InjectorUtils.provideItemListViewModelFactory(requireContext())
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.itemList.observe(viewLifecycleOwner) { itemList ->
            Timber.i("get item list")
            requireActivity().runOnUiThread {
                if (itemList.isNotEmpty()) {
                    (item_list_RecyclerView.adapter as ItemRecyclerViewAdapter).updateList(itemList)
                    hint_TextView.text = ""
                } else {
                    (item_list_RecyclerView.adapter as ItemRecyclerViewAdapter).updateList(ArrayList())
                    hint_TextView.text = "點擊相機新增項目"
                }
            }
        }
        viewModel.updateItemList()
    }

    inner class ItemRecyclerViewAdapter(
        private val context: Context
    ) : RecyclerView.Adapter<ItemRecyclerViewAdapter.ViewHolder>() {

        var list = ArrayList<Item>()
        fun updateList(list: ArrayList<Item>) {
            Timber.d("update item list")
            this.list = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
           return ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.card_view_item, parent, false))
        }
        override fun getItemCount(): Int = list.size
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemView.setOnLongClickListener {
                MaterialAlertDialogBuilder(context)
                    .setMessage("確定要刪除這個物件？")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("確定") { dialog, which ->
                        Thread { viewModel.removeItem(list[position]) }.start()
                    }
                    .setCancelable(false)
                    .show()
                true
            }
            holder.itemImageView.setImageBitmap(fileToBitmap(requireContext(), list[position].itemFileName))
            holder.itemBgImageView.setImageBitmap(fileToBitmap(requireContext(), list[position].backgroundFileName))
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val itemBgImageView: ImageView = view.findViewById(R.id.item_bg_ImageView)
            val itemImageView: ImageView = view.findViewById(R.id.item_ImageView)
        }
    }
}