package com.vedatakcan.inomaker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vedatakcan.inomaker.databinding.FragmentAddImageBinding
import com.vedatakcan.inomaker.adapter.ImageAdapter
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

            addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    if (e.action == MotionEvent.ACTION_DOWN) {
                        val child = rv.findChildViewUnder(e.x, e.y)
                        child?.let {
                            val position = rv.getChildAdapterPosition(it)
                            if (position != RecyclerView.NO_POSITION) {
                                if (e.action == MotionEvent.ACTION_DOWN && e.pointerCount == 1) {
                                    // Uzun basılan öğenin konumunu kontrol et
                                    if (position in selectedImageList.indices) {
                                        showDeleteConfirmationDialog(position)
                                        return true
                                    }
                                }
                            }
                        }
                    }
                    return false
                }

                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
            })
        }


        return binding.root
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.apply {
            setMessage("Resmi silmek istiyor musunuz?")
            setPositiveButton("Sil") { _, _ ->
                // Kullanıcı silmeyi onayladı, resmi sil
                selectedImageList.removeAt(position)
                imageAdapter.notifyItemRemoved(position)
            }
            setNegativeButton("Vazgeç") { dialog, _ ->
                // Kullanıcı vazgeçti, dialogu kapat
                dialog.dismiss()
            }
            create().show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        database = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        loadCategories()

        binding.chooseImage.setOnClickListener {
            uploadPhotos()
        }

        binding.btnAddImage.setOnClickListener {
            saveImagesToFirebaseStorage()
        }

        binding.btnHome.setOnClickListener {
            navController.navigate(R.id.action_addImageFragment_to_optionsFragment)
        }
    }

    private fun loadCategories() {
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
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            if (data.clipData != null) {
                // Birden fazla resim seçildi
                val count = data.clipData!!.itemCount
                val startPosition = selectedImageList.size // Yeni eklenen resimlerin başlangıç pozisyonu
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    selectedImageList.add(imageUri)
                    imageAdapter.notifyItemInserted(startPosition + i) // Yeni eklenen her resmi göster
                }
            } else if (data.data != null) {
                // Tek resim seçildi
                val imageUri = data.data!!
                selectedImageList.add(imageUri)
                imageAdapter.notifyItemInserted(selectedImageList.size - 1) // Yeni eklenen resmi göster
            }

            binding.imRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun saveImagesToFirebaseStorage() {
        // Butonun etkinliğini devre dışı bırak
        binding.btnAddImage.isEnabled = false
        selectedCategoryName?.let { categoryName ->
            val categoryStorageRef = storage.reference.child("category_images/$categoryName")
            val imageUrls = mutableListOf<String>() // URL'leri tutacak liste

            var currentIndex = 0

            // Fonksiyon her çağrıldığında bir resmi yükle ve Firestore'a kaydet
            fun uploadNextImage() {
                if (currentIndex < selectedImageList.size) {
                    val imageUri = selectedImageList[currentIndex]
                    val ref = categoryStorageRef.child("${UUID.randomUUID()}_${currentIndex}.jpg")
                    ref.putFile(imageUri)
                        .addOnSuccessListener { taskSnapshot ->
                            ref.downloadUrl.addOnSuccessListener { url ->
                                val imageUrl = url.toString()
                                imageUrls.add(imageUrl)

                                currentIndex++
                                uploadNextImage() // Bir sonraki resmi yükle
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FirebaseStorage", "Error uploading image", exception)
                        }
                } else {
                    // Tüm resimler yüklendiyse Firestore'a kaydetme işlemine geç
                    saveImageUrlsToFirestore(imageUrls)
                }
            }

            uploadNextImage() // İlk resmi yükleme işlemini başlat
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

                        val categoryImagesRef = database.collection("Categories")
                            .document(categoryId)
                            .collection("CategoryImages")

                        for ((index, imageUrl) in imageUrls.withIndex()) {
                            categoryImagesRef.add(mapOf("imageUrl" to imageUrl, "order" to index))
                                .addOnSuccessListener {
                                    // Resim URL'si başarıyla eklendi
                                    if(index == imageUrls.size - 1){
                                        Toast.makeText(requireContext(), "Resimler başarı ile kaydedildi. ", Toast.LENGTH_SHORT).show()
                                        // Firestore işlemleri yapıldıktan sonra butonun etkinliğini tekrar aktif hale getir
                                        binding.btnAddImage.isEnabled = true
                                        navController.navigate(R.id.action_addImageFragment_to_optionsFragment)
                                    }

                                }
                                .addOnFailureListener { exception ->
                                    // Resim URL'si eklenirken hata oluştu
                                    Log.e("Firestore", "Error adding image URL", exception)
                                }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error getting document", exception)
                }
        }
    }
}
