package com.vedatakcan.inomaker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vedatakcan.inomaker.repositories.CategoriesRepository

class GeneralViewModelFactory(
    private val categoriesRepository: CategoriesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(OptionsViewModel::class.java) -> {
                OptionsViewModel(categoriesRepository) as T
            }
            modelClass.isAssignableFrom(AddCategoryViewModel::class.java) -> {
                AddCategoryViewModel(categoriesRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

