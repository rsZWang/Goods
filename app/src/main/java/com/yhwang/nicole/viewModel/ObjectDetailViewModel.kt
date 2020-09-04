package com.yhwang.nicole.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yhwang.nicole.repository.ObjectRepository

class ObjectDetailViewModel(
    objectRepository: ObjectRepository
) : ViewModel() {

    companion object {
        class ObjectViewModelFactory(
            private val objectRepository: ObjectRepository
        ) : ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return ObjectDetailViewModel(
                    objectRepository
                ) as T
            }
        }
    }

}