package com.vedatakcan.inomaker.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vedatakcan.inomaker.model.CategoriesModel

class CategoriesRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
){

    fun getCategories(onResult: (List<CategoriesModel>) -> Unit) {
        firestore.collection("Categories")
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { result ->
                val categories = result.map { doc ->
                    CategoriesModel(
                        categoryId = doc.id,
                        categoryName = doc.getString("categoryName") ?: "",
                        active = doc.getBoolean("active") ?: false,
                        imageUrl = doc.getString("imageUrl") ?: ""
                    )
                }
                onResult(categories)
            }
    }

    fun deleteCategory(category: CategoriesModel, onComplete: (Boolean) -> Unit) {
        category.categoryId?.let { id ->
            val categoryRef = firestore.collection("Categories").document(id)

            categoryRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("categoryName")
                    val imageUrl = doc.getString("imageUrl")

                    categoryRef.delete().addOnSuccessListener {
                        deleteCategoryImages(id, name, imageUrl)
                        onComplete(true)
                    }.addOnFailureListener {
                        onComplete(false)
                    }
                } else {
                    onComplete(false)
                }
            }
        }
    }

    private fun deleteCategoryImages(categoryId: String, categoryName: String?, imageUrl: String?) {
        val ref = firestore.collection("Categories").document(categoryId).collection("CategoryImages")
        ref.get().addOnSuccessListener { docs ->
            for (doc in docs) {
                doc.reference.delete()
            }
            categoryName?.let { name ->
                storage.reference.child("category_images/$name").listAll()
                    .addOnSuccessListener { items ->
                        items.items.forEach { it.delete() }
                    }
            }
            imageUrl?.let { url ->
                storage.getReferenceFromUrl(url).delete()
            }
        }
    }
}
