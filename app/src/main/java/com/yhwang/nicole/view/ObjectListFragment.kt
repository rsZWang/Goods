package com.yhwang.nicole.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetManager
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
import com.yhwang.nicole.Mode
import com.yhwang.nicole.R
import com.yhwang.nicole.adapter.Object2DListRecyclerViewAdapter
import com.yhwang.nicole.adapter.Object3DListRecyclerViewAdapter
import com.yhwang.nicole.database.GoodsDatabase
import com.yhwang.nicole.model.Object2D
import com.yhwang.nicole.repository.ObjectListRepository
import com.yhwang.nicole.utility.fileToBitmap
import com.yhwang.nicole.viewModel.ObjectListViewModel
import kotlinx.android.synthetic.main.fragment_object_list.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.InputStream


@SuppressLint("SetTextI18n")
class ObjectListFragment : Fragment() {

    private var mode = Mode.OBJECT_2D
    private lateinit var switchModeButton: Button
    private lateinit var object2DListRecyclerView: RecyclerView
    private lateinit var object3DListRecyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_object_list, container, false)

        switchModeButton = view.findViewById<Button>(R.id.switch_mode_Button)
        switchModeButton.setOnClickListener {
            switchMode(when (mode) {
                Mode.OBJECT_2D -> Mode.OBJECT_3D
                Mode.OBJECT_3D -> Mode.OBJECT_2D
            })
        }

        var layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.isItemPrefetchEnabled = true
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        object2DListRecyclerView = view.findViewById(R.id.object_2d_list_RecyclerView)
        object2DListRecyclerView.layoutManager = layoutManager
        object2DListRecyclerView.adapter = Object2DListRecyclerViewAdapter(requireContext(), viewModel, findNavController())

        layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.isItemPrefetchEnabled = true
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        object3DListRecyclerView = view.findViewById(R.id.object_3d_list_RecyclerView)
        object3DListRecyclerView.layoutManager = layoutManager
        object3DListRecyclerView.adapter = Object3DListRecyclerViewAdapter(requireContext(), viewModel, requireContext().assets, findNavController())

        view.findViewById<ImageView>(R.id.to_camera_fragment_Button).setOnClickListener {
            when (mode) {
                Mode.OBJECT_2D -> {
                    val destination = ObjectListFragmentDirections
                        .actionObjectListFragmentToObject2DCameraFragment()
                    findNavController().navigate(destination)
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

        return view
    }

    private val viewModel: ObjectListViewModel by viewModels {
        ObjectListViewModel.Companion.Factory(
            ObjectListRepository(
                requireContext(),
                GoodsDatabase.getInstance(requireContext())!!
            )
        )
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.object2DList.observe(viewLifecycleOwner) { list ->
            Timber.i("get object 2d list")
            requireActivity().runOnUiThread {
                (object2DListRecyclerView.adapter as Object2DListRecyclerViewAdapter).updateList(list)
            }
        }

        viewModel.object3DList.observe(viewLifecycleOwner) { list ->
            Timber.i("get object 3d list")
            requireActivity().runOnUiThread {
                (object3DListRecyclerView.adapter as Object3DListRecyclerViewAdapter).updateList(list)
            }
            switchMode(Mode.OBJECT_3D)
        }

        GlobalScope.launch {
            viewModel.updateObject2DList()
        }

        GlobalScope.launch {
            viewModel.getObject3DList(requireContext().assets!!)
        }
    }

    private fun switchMode(mode: Mode) {
        when (mode) {
            Mode.OBJECT_2D -> {
                this.mode = Mode.OBJECT_2D
                switchModeButton.text = "2D"
                object2DListRecyclerView.visibility = View.VISIBLE
                object3DListRecyclerView.visibility = View.INVISIBLE
                empty_hint_TextView.visibility = if ((object2DListRecyclerView.adapter as Object2DListRecyclerViewAdapter).list.size <= 0) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
            }

            Mode.OBJECT_3D -> {
                this.mode = Mode.OBJECT_3D
                switchModeButton.text = "3D"
                object2DListRecyclerView.visibility = View.INVISIBLE
                object3DListRecyclerView.visibility = View.VISIBLE
                empty_hint_TextView.visibility = if ((object3DListRecyclerView.adapter as Object3DListRecyclerViewAdapter).list.size <= 0) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
            }
        }
    }
}