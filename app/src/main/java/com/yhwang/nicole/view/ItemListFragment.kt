package com.yhwang.nicole.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.findNavController
import com.yhwang.nicole.R

class ItemListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)

        view.findViewById<Button>(R.id.to_camera_fragment_Button).setOnClickListener {
            val destination =
                ItemListFragmentDirections.actionItemListFragmentToCamera2DFragment()
            view.findNavController().navigate(destination)
        }

        return view
    }
}