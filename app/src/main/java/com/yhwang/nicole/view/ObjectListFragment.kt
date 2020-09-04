package com.yhwang.nicole.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yhwang.nicole.R
import com.yhwang.nicole.database.GoodsDatabase
import com.yhwang.nicole.model.Object2D
import com.yhwang.nicole.repository.ObjectListRepository
import com.yhwang.nicole.utility.*
import com.yhwang.nicole.viewModel.ObjectListViewModel
import kotlinx.android.synthetic.main.fragment_object_list.*
import timber.log.Timber
import kotlin.collections.ArrayList

class ObjectListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_object_list, container, false)

        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.isItemPrefetchEnabled = true
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        val objectListRecyclerView = view.findViewById<RecyclerView>(R.id.object_list_RecyclerView)
        objectListRecyclerView.layoutManager = layoutManager
        objectListRecyclerView.adapter = ObjectListRecyclerViewAdapter(requireContext())

        view.findViewById<ImageView>(R.id.to_camera_fragment_Button).setOnClickListener {
            Timber.i("to camera 2D fragment")
//            val destination = ItemListFragmentDirections
//                .actionItemListFragmentToCamera2DFragment()
            val destination = ObjectListFragmentDirections.actionObjectListFragmentToObject3DCameraFragment()
            findNavController().navigate(destination)
        }

        return view
    }

    private val viewModel: ObjectListViewModel by viewModels {
        ObjectListViewModel.Companion.Factory(
            ObjectListRepository(
                requireContext(),
                GoodsDatabase.getInstance(requireContext())!!)
        )
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.object2DList.observe(viewLifecycleOwner) { objectList ->
            Timber.i("get object 2d list")
            requireActivity().runOnUiThread {
                if (objectList.isNotEmpty()) {
                    (object_list_RecyclerView.adapter as ObjectListRecyclerViewAdapter).updateList(objectList)
//                    hint_TextView.text = ""
                } else {
                    (object_list_RecyclerView.adapter as ObjectListRecyclerViewAdapter).updateList(ArrayList())
//                    hint_TextView.text = "點擊相機新增項目"
                }
            }
        }
        viewModel.updateObject2DList()
    }

    inner class ObjectListRecyclerViewAdapter(
        private val context: Context
    ) : RecyclerView.Adapter<ObjectListRecyclerViewAdapter.ViewHolder>() {

        var list = ArrayList<Object2D>()
        fun updateList(list: ArrayList<Object2D>) {
            Timber.d("update object 2d list")
            this.list = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
           return ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.card_view_object, parent, false))
        }
        override fun getItemCount(): Int = list.size
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemView.setOnLongClickListener {
                MaterialAlertDialogBuilder(context)
                    .setMessage("確定要刪除這個物件？")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("確定") { _, _ ->
                        Thread { viewModel.removeObject(list[position]) }.start()
                    }
                    .setCancelable(false)
                    .show()
                true
            }
            holder.itemView.setOnClickListener {
                Timber.i("pass object: %d", list[position].id)
                val destination = ObjectListFragmentDirections
                    .actionObjectListFragmentToObjectViewFragment(list[position])
                findNavController().navigate(destination)
            }
            holder.objectBgImageView.setImageBitmap(fileToBitmap(requireContext(), list[position].backgroundFileName))
            holder.objectImageView.setImageBitmap(fileToBitmap(requireContext(), list[position].objectFileName))
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val objectBgImageView: ImageView = view.findViewById(R.id.object_bg_ImageView)
            val objectImageView: ImageView = view.findViewById(R.id.object_ImageView)
        }
    }
}