package com.yhwang.nicole.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.easystudio.rotateimageview.RotateZoomImageView
import com.yhwang.nicole.R
import com.yhwang.nicole.model.Item
import com.yhwang.nicole.utilities.*
import com.yhwang.nicole.viewModel.ItemListViewModel
import kotlinx.android.synthetic.main.fragment_item_list.*
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList


class ItemListFragment : Fragment() {

    companion object {
        fun newInstance() = ItemListFragment()
    }

    lateinit var adapter: ItemRecyclerViewAdapter
    lateinit var itemBgRelativeLayout: RelativeLayout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)

        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        val itemListRecyclerView = view.findViewById<RecyclerView>(R.id.item_list_RecyclerView)
        itemListRecyclerView.layoutManager = layoutManager
        adapter = ItemRecyclerViewAdapter(requireContext())
        itemListRecyclerView.adapter = adapter

        itemBgRelativeLayout = view.findViewById(R.id.item_bg_RelativeLayout)

        view.findViewById<ImageView>(R.id.to_camera_fragment_Button).setOnClickListener {
            Timber.d("to camera 2D fragment")
            val destination = ItemListFragmentDirections
                .actionItemListFragmentToCamera2DFragment()
            view.findNavController().navigate(destination)
        }

        return view
    }

    private val viewModel: ItemListViewModel by viewModels {
        InjectorUtils.provideItemListViewModelFactory(requireContext())
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getItemList().observe(viewLifecycleOwner) {
            Timber.i("get item list")
            if (it.isNotEmpty()) {
                hint_TextView.text = ""
                drawItemCardView(it, 0)
            } else {
                Timber.i("NO item now")
//                defaultCardView = LayoutInflater.from(requireContext()).inflate(R.layout.card_view_item_default, null)
//                itemBgRelativeLayout.addView(defaultCardView)
//                Timer().schedule(object : TimerTask() {
//                    override fun run() {
//                        requireActivity().runOnUiThread {
//                            adapter.list.add(layoutToDrawable(resources, itemBgRelativeLayout))
//                            adapter.notifyDataSetChanged()
//                            itemBgRelativeLayout.removeAllViews()
//                        }
//                    }
//                }, 5)
            }
        }
    }


    fun drawItemCardView(itemList: List<Item>, index: Int) {
        try {
            Timber.i("draw item card view")
            val itemRotateZoomImageView = RotateZoomImageView(requireContext())
            itemRotateZoomImageView.setImageBitmap(fileToBitmap(requireContext(), itemList[index].itemFileName))
            itemBgRelativeLayout.addView(itemRotateZoomImageView)
            itemBgRelativeLayout.background = BitmapDrawable(resources, fileToBitmap(requireContext(), itemList[index].backgroundFileName))
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    try {
                        requireActivity().runOnUiThread {
                            if (itemBgRelativeLayout.width > 0 && itemBgRelativeLayout.height > 0) {
                                adapter.list.add(layoutToDrawable(resources, itemBgRelativeLayout))
                                adapter.notifyDataSetChanged()
                                itemBgRelativeLayout.removeAllViews()
                            } else {
                                Timber.e("error item width/height equals 0")
                            }

                            if (index<itemList.size-1) {
                                drawItemCardView(itemList, index+1)
                            } else {
                                itemBgRelativeLayout.visibility = View.INVISIBLE
                            }
                        }
                    } catch (exception: IllegalStateException) {
                        exception.printStackTrace()
                        return
                    }
                }
            }, 5)
        } catch (exception: IllegalStateException) {
            exception.printStackTrace()
            return
        }
    }

    class ItemRecyclerViewAdapter(
        private val context: Context
    ) : RecyclerView.Adapter<ItemRecyclerViewAdapter.ViewHolder>() {

        var list = ArrayList<Drawable>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
           return ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.card_view_item, parent, false))
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.imageView.setImageDrawable(list[position])
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.item_ImageView)
        }
    }
}