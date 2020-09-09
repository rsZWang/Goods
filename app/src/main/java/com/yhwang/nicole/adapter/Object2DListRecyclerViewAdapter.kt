package com.yhwang.nicole.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yhwang.nicole.R
import com.yhwang.nicole.model.Object2D
import com.yhwang.nicole.utility.fileToBitmap
import com.yhwang.nicole.view.ObjectListFragmentDirections
import com.yhwang.nicole.viewModel.ObjectListViewModel
import timber.log.Timber

class Object2DListRecyclerViewAdapter(
    private val context: Context,
    private val viewModel: ObjectListViewModel,
    private val navController: NavController
) : RecyclerView.Adapter<Object2DListRecyclerViewAdapter.ViewHolder>() {

    var list = ArrayList<Object2D>()

    fun updateList(list: ArrayList<Object2D>) {
        Timber.i("update object 2d list")
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.card_view_object, parent, false)
        )
    }
    override fun getItemCount(): Int = list.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnLongClickListener {
            MaterialAlertDialogBuilder(context)
                .setMessage("確定要刪除這個物件？")
                .setNegativeButton("取消", null)
                .setPositiveButton("確定") { _, _ ->
//                    Thread { viewModel.removeObject2D(list[position]) }.start()
                }
                .setCancelable(false)
                .show()
            true
        }
        holder.itemView.setOnClickListener {
            Timber.i("pass object: %d", list[position].id)
            val destination = ObjectListFragmentDirections
                .actionObjectListFragmentToObjectViewFragment(list[position], null)
            navController.navigate(destination)
        }
        holder.objectBgImageView.setImageBitmap(
            fileToBitmap(
                context,
                list[position].backgroundFileName
            )
        )
        holder.objectImageView.setImageBitmap(
            fileToBitmap(
                context,
                list[position].objectFileName
            )
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val objectBgImageView: ImageView = view.findViewById(R.id.object_bg_ImageView)
        val objectImageView: ImageView = view.findViewById(R.id.object_ImageView)
    }
}