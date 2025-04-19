package com.vedatakcan.inomaker

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

class ImageRepository(private val firestore: FirebaseFirestore) {

    fun getImagesForCategory(categoryId: String): LiveData<List<String>> {
        val imageListLiveData = MutableLiveData<List<String>>()
        val imageList = mutableListOf<String>()

        firestore.collection("Categories")
            .document(categoryId)
            .collection("CategoryImages")
            .orderBy("order")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val imageUrl = document.getString("imageUrl")
                    imageUrl?.let { imageList.add(it) }
                }
                imageListLiveData.value = imageList
            }
            .addOnFailureListener { e ->
                Log.e("ImageRepository", "Error fetching images", e)
            }

        return imageListLiveData
    }
}
