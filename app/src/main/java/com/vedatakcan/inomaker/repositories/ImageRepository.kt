package com.vedatakcan.inomaker.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ImageRepository(private val firestore: FirebaseFirestore) {

    fun getImagesForCategory(categoryId: String): Flow<List<String>> = callbackFlow {
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
                trySend(imageList).isSuccess
            }
            .addOnFailureListener {
                close(it)
            }

        awaitClose { /* No-op */ }
    }
}
