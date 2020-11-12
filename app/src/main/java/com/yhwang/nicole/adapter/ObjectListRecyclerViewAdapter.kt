package com.yhwang.nicole.adapter

import android.content.Context
import android.content.res.AssetManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.yhwang.nicole.R
import com.yhwang.nicole.utility.assetsImageToBitmap
import com.yhwang.nicole.utility.fileToBitmap
import com.yhwang.nicole.view.ObjectListFragment
import com.yhwang.nicole.view.ObjectListFragmentDirections
import com.yhwang.nicole.viewModel.ObjectListViewModel
import timber.log.Timber

class ObjectListRecyclerViewAdapter(
    private val context: Context,
    private val assetManager: AssetManager,
    private val navController: NavController,
    private val objectList: ArrayList<ObjectListFragment.Object>
) : RecyclerView.Adapter<ObjectListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.card_view_object, parent, false)
        )
    }

    fun updateList(list: List<ObjectListFragment.Object>) {
        objectList.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = objectList.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val obj = objectList[position]
        when (obj.dimension) {
            ObjectListFragment.ObjectDimension.Object2D -> {
                holder.itemView.setOnClickListener {
                    navController.navigate(ObjectListFragmentDirections
                        .actionObjectListFragmentToObjectViewFragment(object2D = obj.object2D!!, object3D = null))
                }
                holder.dimensionTextView.text = "2D"
                holder.objectBgImageView.setImageBitmap(
                    fileToBitmap(context, obj.object2D!!, isBackground = true)
                )
                holder.objectImageView.setImageBitmap(
                    fileToBitmap(context, obj.object2D, isBackground = false)
                )
            }

            ObjectListFragment.ObjectDimension.Object3D -> {
                holder.itemView.setOnClickListener {
                    navController.navigate(ObjectListFragmentDirections
                        .actionObjectListFragmentToObjectViewFragment(object2D = null, object3D = obj.object3D))
                }
                holder.dimensionTextView.text = "3D"
                holder.objectBgImageView.setImageBitmap(assetsImageToBitmap(assetManager, "${obj.object3D}_original.jpeg"))
                holder.objectImageView.setImageBitmap(null)
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dimensionTextView: TextView = view.findViewById(R.id.dimension_TextView)
        val objectBgImageView: ImageView = view.findViewById(R.id.object_bg_ImageView)
        val objectImageView: ImageView = view.findViewById(R.id.object_ImageView)
    }
}