package com.vedatakcan.inomaker.repositories

import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AddImageRepository(
    private val storage: FirebaseStorage,
    private val database: FirebaseFirestore
) {

    fun getCategories(onComplete: (List<String>) -> Unit, onError: (Exception) -> Unit) {
        database.collection("Categories").get()
            .addOnSuccessListener { result ->
                val categoryList = result.mapNotNull { it.getString("categoryName") }
                onComplete(categoryList)
            }
            .addOnFailureListener { onError(it) }
    }

    fun uploadImagesToStorage(
        imageUris: List<Uri>,
        categoryName: String,
        onProgress: (Int) -> Unit,
        onComplete: (List<String>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val categoryStorageRef = storage.reference.child("category_images/$categoryName")
        val imageUrls = mutableListOf<String>()
        var currentIndex = 0

        fun uploadNext() {
            if (currentIndex < imageUris.size) {
                val imageUri = imageUris[currentIndex]
                val ref = categoryStorageRef.child("${UUID.randomUUID()}_$currentIndex.jpg")

                ref.putFile(imageUri)
                    .addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener { uri ->
                            imageUrls.add(uri.toString())
                            onProgress(currentIndex + 1)
                            currentIndex++
                            uploadNext()
                        }
                    }
                    .addOnFailureListener { onError(it) }
            } else {
                onComplete(imageUrls)
            }
        }

        uploadNext()
    }

    fun saveImageUrlsToFirestore(
        categoryName: String,
        imageUrls: List<String>,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        database.collection("Categories")
            .whereEqualTo("categoryName", categoryName)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.firstOrNull()?.let { document ->
                    val categoryImagesRef = database.collection("Categories")
                        .document(document.id)
                        .collection("CategoryImages")

                    val tasks = imageUrls.mapIndexed { index, url ->
                        categoryImagesRef.add(mapOf("imageUrl" to url, "order" to index))
                    }

                    Tasks.whenAllSuccess<Any>(tasks)
                        .addOnSuccessListener { onComplete() }
                        .addOnFailureListener { onError(it) }
                } ?: onError(Exception("Kategori bulunamadı"))
            }
            .addOnFailureListener { onError(it) }
    }
}
