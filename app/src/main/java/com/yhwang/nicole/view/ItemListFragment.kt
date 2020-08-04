package com.yhwang.nicole.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.yhwang.nicole.R
import com.yhwang.nicole.utilities.InjectorUtils
import com.yhwang.nicole.viewModel.ItemListViewModel

class ItemListFragment : Fragment() {

    companion object {
        fun newInstance() = ItemListFragment()
    }

    private val viewModel: ItemListViewModel by viewModels {
        InjectorUtils.provideItemListViewModeFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_item_list, container, false)
    }

}