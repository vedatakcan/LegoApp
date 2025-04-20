package com.vedatakcan.inomaker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vedatakcan.inomaker.repositories.ImageRepository

class ImageViewModel(private val repository: ImageRepository) : ViewModel() {

    private val _images = MutableLiveData<List<String>>()
    val images: LiveData<List<String>> = _images

    fun fetchImages(categoryId: String) {
        repository.getImagesForCategory(categoryId).observeForever { imageList ->
            // Aynı liste tekrar geliyorsa set etme
            if (_images.value != imageList) {
                _images.value = imageList
            }
        }
    }
}
