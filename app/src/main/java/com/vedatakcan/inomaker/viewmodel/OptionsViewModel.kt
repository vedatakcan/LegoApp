package com.vedatakcan.inomaker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vedatakcan.inomaker.model.CategoriesModel
import com.vedatakcan.inomaker.repositories.CategoriesRepository


class OptionsViewModel(private val repository: CategoriesRepository) : ViewModel() {

    private val _categories = MutableLiveData<List<CategoriesModel>>()
    val categories: LiveData<List<CategoriesModel>> = _categories

    fun fetchCategories() {
        repository.getCategories { list ->
            _categories.postValue(list)
        }
    }

    fun deleteCategory(category: CategoriesModel, onComplete: (Boolean) -> Unit) {
        repository.deleteCategory(category, onComplete)
    }

    fun filterCategories(query: String): List<CategoriesModel> {
        return _categories.value?.filter {
            it.categoryName.contains(query, ignoreCase = true)
        } ?: emptyList()
    }
}
