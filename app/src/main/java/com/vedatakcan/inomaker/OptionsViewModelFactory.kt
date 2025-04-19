package com.vedatakcan.inomaker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vedatakcan.inomaker.repositories.CategoriesRepository

class OptionsViewModelFactory(private val repository: CategoriesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OptionsViewModel::class.java)) {
            return OptionsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
