package com.vedatakcan.inomaker.repositories

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vedatakcan.inomaker.model.CategoriesModel
import java.util.UUID

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

    fun addCategory(categoryName: String, isActive: Boolean, imageUri: Uri?, onComplete: (Boolean) -> Unit) {
        if (imageUri != null) {
            // Resim adı oluştur
            val imageName = "${UUID.randomUUID()}.jpg"

            // Resmi Firebase Storage'a yükle
            val storageRef = storage.reference.child("category_images/$imageName")
            storageRef.putFile(imageUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { url ->
                        // Kategoriyi Firestore'a kaydet
                        saveCategoryToFirestore(categoryName, isActive, url.toString(), onComplete)
                    }
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        } else {
            // Resim yoksa sadece kategori kaydını ekle
            saveCategoryToFirestore(categoryName, isActive, "", onComplete)
        }
    }

    private fun saveCategoryToFirestore(categoryName: String, isActive: Boolean, imageUrl: String, onComplete: (Boolean) -> Unit) {
        val category = CategoriesModel(categoryName = categoryName, active = isActive, imageUrl = imageUrl)
        firestore.collection("Categories")
            .add(category)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }


}
