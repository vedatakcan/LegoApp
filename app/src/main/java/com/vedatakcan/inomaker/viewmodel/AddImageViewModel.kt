package com.vedatakcan.inomaker.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vedatakcan.inomaker.repositories.AddImageRepository

class AddImageViewModel(private val repository: AddImageRepository) : ViewModel() {

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> get() = _categories

    private val _uploadProgress = MutableLiveData<Int>()
    val uploadProgress: LiveData<Int> get() = _uploadProgress

    private val _uploadComplete = MutableLiveData<Boolean>()
    val uploadComplete: LiveData<Boolean> get() = _uploadComplete

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun fetchCategories() {
        repository.getCategories(
            onComplete = { _categories.value = it },
            onError = { _error.value = it.message }
        )
    }

    fun uploadImages(categoryName: String, imageUris: List<Uri>) {
        repository.uploadImagesToStorage(
            imageUris,
            categoryName,
            onProgress = { _uploadProgress.postValue(it) },
            onComplete = { imageUrls ->
                saveImageUrls(categoryName, imageUrls)
            },
            onError = { _error.postValue(it.message) }
        )
    }

    private fun saveImageUrls(categoryName: String, imageUrls: List<String>) {
        repository.saveImageUrlsToFirestore(
            categoryName,
            imageUrls,
            onComplete = { _uploadComplete.postValue(true) },
            onError = { _error.postValue(it.message) }
        )
    }
}
