package com.yhwang.nicole.view

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.findNavController
import com.yhwang.nicole.R
import com.yhwang.nicole.viewModel.StartPageViewModel
import timber.log.Timber

class StartPageFragment : Fragment() {

    companion object {
        fun newInstance() = StartPageFragment()
    }

    private lateinit var viewModel: StartPageViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_start_page, container, false)

        return inflater.inflate(R.layout.fragment_start_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.to_item_list_fragment_Button).setOnClickListener {
            Timber.d("to item list fragment")
            val destination = StartPageFragmentDirections
                .actionStartPageFragment2ToItemListFragment()
            view.findNavController().navigate(destination)
        }

        view.findViewById<Button>(R.id.to_camera_fragment_Button).setOnClickListener {
            Timber.d("to camera 2D fragment")
            val destination = StartPageFragmentDirections
                .actionStartPageFragment2ToCamera2DFragment()
            view.findNavController().navigate(destination)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(StartPageViewModel::class.java)
        // TODO: Use the ViewModel
    }

}