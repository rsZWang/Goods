package com.yhwang.nicole.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.easystudio.rotateimageview.RotateZoomImageView
import com.yhwang.nicole.R
import com.yhwang.nicole.utilities.InjectorUtils
import com.yhwang.nicole.viewModel.ItemListViewModel
import kotlinx.android.synthetic.main.fragment_camera_2d.*
import kotlinx.android.synthetic.main.fragment_item_list.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class ItemListFragment : Fragment() {

    companion object {
        fun newInstance() = ItemListFragment()
    }

    private val viewModel: ItemListViewModel by viewModels {
        InjectorUtils.provideItemListViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)
        return view
    }

    lateinit var itemBitmap: Bitmap
    lateinit var itemBackgroundBitmap: Bitmap
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.itemDrawableList.observe(viewLifecycleOwner) { itemDrawableList ->
            val itemListRecyclerView = view.findViewById<RecyclerView>(R.id.item_list_RecyclerView)
            val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
            itemListRecyclerView.layoutManager = layoutManager
            itemListRecyclerView.adapter = ItemRecyclerViewAdapter(requireContext(), itemDrawableList)
        }

        viewModel.itemList.observe(viewLifecycleOwner) {
            itemBitmap = loadJpgFileToBitmap(it[0].itemFileName)
            itemBackgroundBitmap = loadJpgFileToBitmap(it[0].backgroundFileName)
            relativeLayout.background = BitmapDrawable(resources, itemBackgroundBitmap)

            val rotateZoomImageView = RotateZoomImageView(requireContext())
            rotateZoomImageView.setImageBitmap(itemBitmap)
            rotateZoomImageView.setOnTouchListener { view, motionEvent -> rotateZoomImageView.onTouch(view, motionEvent) }
            if (Build.VERSION.SDK_INT >= 24){
                rotateZoomImageView.updateDragShadow(View.DragShadowBuilder(rotateZoomImageView))
            }
            val width = relativeLayout.width/3*2
            val height = relativeLayout.height/3*2
            val x = it[0].x
            val y = it[0].y
            val layoutParams = RelativeLayout.LayoutParams(width, height)
            layoutParams.marginStart = x.toInt()
            layoutParams.topMargin = y.toInt()
            relativeLayout.addView(rotateZoomImageView, layoutParams)
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

    private fun loadJpgFileToBitmap(fileName: String) : Bitmap {
        // Initialize a new file instance to save bitmap object
        var file = ContextWrapper(context).getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE)
        file = File(file, "$fileName.jpg")
        return BitmapFactory.decodeFile(file.path)
    }
}