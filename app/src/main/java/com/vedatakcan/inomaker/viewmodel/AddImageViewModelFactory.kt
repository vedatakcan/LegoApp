package com.vedatakcan.inomaker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vedatakcan.inomaker.repositories.AddImageRepository

class AddImageViewModelFactory(
    private val repository: AddImageRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddImageViewModel::class.java)) {
            return AddImageViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
