package com.vedatakcan.inomaker

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vedatakcan.inomaker.repositories.CategoriesRepository
import java.util.UUID

class AddCategoryViewModel(private val repository: CategoriesRepository) : ViewModel() {

    private val _categoryAdded = MutableLiveData<Boolean>()
    val categoryAdded: LiveData<Boolean> get() = _categoryAdded

    private val _isImageValid = MutableLiveData<Boolean>()
    val isImageValid: LiveData<Boolean> get() = _isImageValid

    private val _isCategoryValid = MutableLiveData<Boolean>()
    val isCategoryValid: LiveData<Boolean> get() = _isCategoryValid

    fun validateCategoryName(categoryName: String) {
        _isCategoryValid.value = categoryName.isNotEmpty() && categoryName.isNotBlank()
    }

    fun validateImage(imageUri: Uri?) {
        _isImageValid.value = imageUri != null
    }

    fun addCategory(categoryName: String, isActive: Boolean, imageUri: Uri?) {
        if (_isCategoryValid.value == true && _isImageValid.value == true) {
            val imageName = "${UUID.randomUUID()}.jpg"
            repository.addCategory(categoryName, isActive, imageUri, object : (Boolean) -> Unit {
                override fun invoke(success: Boolean) {
                    _categoryAdded.value = success
                }
            })
        } else {
            _categoryAdded.value = false
        }
    }
}
