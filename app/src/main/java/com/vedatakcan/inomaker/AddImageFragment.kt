package com.vedatakcan.inomaker

import android.R
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.vedatakcan.inomaker.databinding.FragmentAddImageBinding
import java.util.UUID


class AddImageFragment : Fragment() {

    private lateinit var storage: FirebaseStorage
    private lateinit var binding: FragmentAddImageBinding
    private lateinit var database: FirebaseFirestore
    private lateinit var navController: NavController


    private var selectedCategoryName: String? = null
    private var selectedImageList: MutableList<Uri> = mutableListOf()
    private lateinit var imageAdapter: ImageAdapter

    companion object {
        private const val GALLERY_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddImageBinding.inflate(inflater, container, false)

        binding.imRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            imageAdapter = ImageAdapter(selectedImageList)
            adapter = imageAdapter
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        database = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()



        // Kategori seçimi yapmak için spinner'ı doldur
        loadCategories()

        // RecyclerView için adapter'ı tanımla
        //imageAdapter = ImageAdapter(selectedImageList)
        //recyclerView.adapter = imageAdapter

        binding.chooseImage.setOnClickListener {
            uploadPhotos()
        }

        // Kullanıcı resimleri onayladıktan sonra kaydetmek istiyorsanız
        binding.btnAddImage.setOnClickListener {
            saveImagesToFirebaseStorage()
        }
    }

    private fun loadCategories() {
        // Firebase'den kategorileri al ve spinner'a ekle
        val categoriesRef = database.collection("Categories")
        categoriesRef.get()
            .addOnSuccessListener { result ->
                val categoryList = mutableListOf<String>()
                for (document in result) {
                    val categoryName = document.getString("categoryName")
                    categoryName?.let {
                        categoryList.add(it)
                    }
                }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerClick.adapter = adapter

                // Kategori seçildiğinde güncelle
                binding.spinnerClick.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedCategoryName = parent?.getItemAtPosition(position).toString()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Hiçbir şey seçilmediğinde yapılacak işlemler
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Kategoriler alınamadı.", Toast.LENGTH_LONG).show()
            }
    }

    private fun uploadPhotos() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage = data.data
            selectedImage?.let {
                selectedImageList.add(it)
                // İlgili UI güncellemeleri yapabilirsiniz
                binding.imRecyclerView.visibility = View.VISIBLE
                imageAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun saveImagesToFirebaseStorage() {
        selectedCategoryName?.let { categoryName ->
            val categoryStorageRef = storage.reference.child("category_images/$categoryName")

            val imageUrls = mutableListOf<String>() // URL'leri tutacak liste

            for ((index, imageUri) in selectedImageList.withIndex()) {
                val ref = categoryStorageRef.child("${UUID.randomUUID()}_${index}.jpg")
                ref.putFile(imageUri)
                    .addOnSuccessListener { taskSnapshot ->
                        ref.downloadUrl.addOnSuccessListener { url ->
                            val imageUrl = url.toString()
                            imageUrls.add(imageUrl) // URL'leri listeye ekle
                            if (imageUrls.size == selectedImageList.size) {
                                // Eğer tüm resimler yüklendiyse Firestore'a kaydetme işlemi başlat
                                saveImageUrlsToFirestore(imageUrls)
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Hata durumunda yapılacak işlemler
                        Log.e("FirebaseStorage", "Error uploading image", exception)
                    }
            }
        }
    }

    private fun saveImageUrlsToFirestore(imageUrls: List<String>) {
        selectedCategoryName?.let { categoryName ->
            val categoryRef = database.collection("Categories")
                .whereEqualTo("categoryName", categoryName)

            categoryRef.get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        val categoryId = document.id

                        // Yeni bir koleksiyon oluştur ve resim URL'lerini buraya ekle
                        val categoryImagesRef = database.collection("Categories")
                            .document(categoryId)
                            .collection("CategoryImages") // Yeni koleksiyon adı

                        // Her bir resim URL'sini yeni koleksiyona ekle
                        for (imageUrl in imageUrls) {
                            categoryImagesRef.add(mapOf("imageUrl" to imageUrl))
                                .addOnSuccessListener {
                                    // Resim URL'si başarıyla eklendi
                                }
                                .addOnFailureListener { exception ->
                                    // Resim URL'si eklenirken hata oluştu
                                    Log.e("Firestore", "Error adding image URL", exception)
                                }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Kategori bulunurken hata oluştu
                    Log.e("Firestore", "Error getting document", exception)
                }
        }
    }


}

