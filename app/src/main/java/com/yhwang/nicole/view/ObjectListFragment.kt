package com.yhwang.nicole.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yhwang.nicole.BuildConfig
import com.yhwang.nicole.Mode
import com.yhwang.nicole.R
import com.yhwang.nicole.adapter.Object2DListRecyclerViewAdapter
import com.yhwang.nicole.adapter.ObjectListRecyclerViewAdapter
import com.yhwang.nicole.database.GoodsDatabase
import com.yhwang.nicole.model.Object2D
import com.yhwang.nicole.repository.ObjectListRepository
import com.yhwang.nicole.viewModel.ObjectListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

@SuppressLint("SetTextI18n")
class ObjectListFragment : Fragment() {

    private val viewModel: ObjectListViewModel by viewModels {
        ObjectListViewModel.Companion.Factory(
            ObjectListRepository(
                requireContext(),
                GoodsDatabase.getInstance(requireContext())!!
            )
        )
    }

    private var rootView: View? = null
    private var mode = Mode.OBJECT_2D
    private lateinit var switchModeButton: Button
    private lateinit var object2DListRecyclerView: RecyclerView
    private lateinit var object3DListRecyclerView: RecyclerView
    private lateinit var objectListRecyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_object_list, container, false)
//            switchModeButton = rootView!!.findViewById(R.id.switch_mode_Button)
//            switchModeButton.setOnClickListener {
//                switchMode(when (mode) {
//                    Mode.OBJECT_2D -> Mode.OBJECT_3D
//                    Mode.OBJECT_3D -> Mode.OBJECT_2D
//                })
//            }

//            var layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
//            layoutManager.isItemPrefetchEnabled = true
//            layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
//            object2DListRecyclerView = rootView!!.findViewById(R.id.object_2d_list_RecyclerView)
//            object2DListRecyclerView.layoutManager = layoutManager
//            object2DListRecyclerView.adapter = Object2DListRecyclerViewAdapter(requireContext(), viewModel, findNavController())
//
//            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
//            layoutManager.isItemPrefetchEnabled = true
//            layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
//            object3DListRecyclerView = rootView!!.findViewById(R.id.object_3d_list_RecyclerView)
//            object3DListRecyclerView.layoutManager = layoutManager
//            object3DListRecyclerView.adapter = Object3DListRecyclerViewAdapter(requireContext(), viewModel, requireContext().assets, findNavController())

//            val progressDialog = showProgressDialog(requireContext())
//            viewModel.updateObject2D(requireActivity().assets.list("object_2d")!!) {
//                loadObject2DList()
//                progressDialog.dismiss()
//            }
//
//            viewModel.object3DList.observe(viewLifecycleOwner) { list ->
//                Timber.i("get object 3d list")
//                GlobalScope.launch(Dispatchers.Main) {
//                    (object3DListRecyclerView.adapter as Object3DListRecyclerViewAdapter).updateList(list)
//                }
//                switchMode(Mode.OBJECT_3D)
//            }

            val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            layoutManager.isItemPrefetchEnabled = true
            layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
            objectListRecyclerView = rootView!!.findViewById(R.id.object_3d_list_RecyclerView)
            objectListRecyclerView.layoutManager = layoutManager

            var objectList: ArrayList<Object>? = null
            var visibleItemCount: Int
            var totalItemCount: Int
            var pastVisibleItems = 0
            objectListRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    visibleItemCount = layoutManager.childCount
                    totalItemCount = layoutManager.itemCount
                    var firstVisibleItems: IntArray? = null
                    firstVisibleItems = layoutManager.findFirstVisibleItemPositions(firstVisibleItems)
                    if(firstVisibleItems != null && firstVisibleItems.size > 0) {
                        pastVisibleItems = firstVisibleItems[0];
                    }
                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        (objectListRecyclerView.adapter as ObjectListRecyclerViewAdapter).updateList(objectList!!)
                    }
                }
            })
            viewModel.objectList.observe(viewLifecycleOwner) { list ->
                objectList = ArrayList(list)
                objectListRecyclerView.adapter = ObjectListRecyclerViewAdapter(
                    requireContext(),
                    requireActivity().assets,
                    findNavController(),
                    objectList!!
                )
            }
            GlobalScope.launch(Dispatchers.IO) {
//                viewModel.loadObject3DList(requireContext().assets!!)
                viewModel.loadObjectList(
                    requireActivity().assets.list("object_2d")!!,
                    requireContext().assets!!
                )
            }

            rootView!!.findViewById<ImageView>(R.id.to_camera_fragment_Button).setOnClickListener {
                when (mode) {
                    Mode.OBJECT_2D -> {
                        findNavController().navigate(
                            ObjectListFragmentDirections
                                .actionObjectListFragmentToObject2DCameraFragment()
                        )
                    }

                    Mode.OBJECT_3D -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setMessage("目前暫時不支援3D拍照")
                            .setPositiveButton("OK", null)
                            .setCancelable(false)
                            .show()
                    }
                }
            }

            rootView!!.findViewById<TextView>(R.id.version_textView).text = BuildConfig.VERSION_NAME
        }

        return rootView
    }

    private fun loadObject2DList() {
        GlobalScope.launch(Dispatchers.Main) {
            viewModel.object2DList.observe(viewLifecycleOwner) { list ->
                Timber.i("Get object 2d list")
                GlobalScope.launch(Dispatchers.Main) {
                    (object2DListRecyclerView.adapter as Object2DListRecyclerViewAdapter).updateList(
                        list
                    )
                }
            }
            GlobalScope.launch {
                viewModel.loadObject2DList()
            }
        }
    }

    private fun switchMode(mode: Mode) {
        when (mode) {
            Mode.OBJECT_2D -> {
                this.mode = Mode.OBJECT_2D
                switchModeButton.text = "2D"
                object2DListRecyclerView.visibility = View.VISIBLE
                object3DListRecyclerView.visibility = View.INVISIBLE
//                empty_hint_TextView.visibility = if ((object2DListRecyclerView.adapter as Object2DListRecyclerViewAdapter).list.size <= 0) {
//                    View.VISIBLE
//                } else {
//                    View.INVISIBLE
//                }
            }

            Mode.OBJECT_3D -> {
                this.mode = Mode.OBJECT_3D
                switchModeButton.text = "3D"
                object2DListRecyclerView.visibility = View.INVISIBLE
                object3DListRecyclerView.visibility = View.VISIBLE
//                empty_hint_TextView.visibility = if ((object3DListRecyclerView.adapter as Object3DListRecyclerViewAdapter).list.size <= 0) {
//                    View.VISIBLE
//                } else {
//                    View.INVISIBLE
//                }
            }
        }
    }

    enum class ObjectDimension {
        Object2D, Object3D
    }

    data class Object(
        val dimension: ObjectDimension,
        val object2D: Object2D? = null,
        val object3D: String? = null,
        val holderView: View? = null
    )
}