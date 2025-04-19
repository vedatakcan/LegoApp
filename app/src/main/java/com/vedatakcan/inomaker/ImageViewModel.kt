package com.vedatakcan.inomaker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ImageViewModel(private val repository: ImageRepository) : ViewModel() {

    private val _images = MutableLiveData<List<String>>()
    val images: LiveData<List<String>> = _images

    fun fetchImages(categoryId: String) {
        repository.getImagesForCategory(categoryId).observeForever {
            _images.value = it
        }
    }
}
