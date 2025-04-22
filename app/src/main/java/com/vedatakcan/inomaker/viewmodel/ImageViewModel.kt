package com.vedatakcan.inomaker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vedatakcan.inomaker.repositories.ImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ImageViewModel(private val repository: ImageRepository) : ViewModel() {
    private val _images = MutableStateFlow<List<String>>(emptyList())
    val images: StateFlow<List<String>> = _images.asStateFlow()

    fun fetchImages(categoryId: String) {
        viewModelScope.launch {
            repository.getImagesForCategory(categoryId).collect { imageList ->
                _images.value = imageList
            }
        }
    }
}

