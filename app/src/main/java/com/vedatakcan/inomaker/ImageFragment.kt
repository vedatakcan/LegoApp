package com.vedatakcan.inomaker

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.vedatakcan.inomaker.databinding.FragmentImageBinding


class ImageFragment : Fragment() {

    private lateinit var binding: FragmentImageBinding
    private lateinit var navController: NavController
    private lateinit var database: FirebaseFirestore
    private lateinit var categoryId: String
    private lateinit var storage: FirebaseStorage
    private lateinit var imageList: List<String> // String tipinde
    private var currentImageIndex = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        database = FirebaseFirestore.getInstance()

        binding.btnHome.setOnClickListener {
            navController.navigate(R.id.action_imageFragment_to_optionsFragment)
        }
        binding.btnBack.setOnClickListener {
            showPreviousImage()
        }
        binding.btnNext.setOnClickListener {
            showNextImage()
        }

        categoryId = arguments?.getString("categoryId").toString()

        if (categoryId.isNotEmpty()) {
            loadImagesForCategory(categoryId)
        }
    }

    private fun showNextImage() {
        if (currentImageIndex < imageList.size - 1) {
            currentImageIndex++
            loadImage(Uri.parse(imageList[currentImageIndex])) // String'ten Uri'ye dönüştürme
        }
    }

    private fun showPreviousImage() {
        if (currentImageIndex > 0) {
            currentImageIndex--
            loadImage(Uri.parse(imageList[currentImageIndex])) // String'ten Uri'ye dönüştürme
        }
    }

    private fun loadImagesForCategory(categoryId: String) {
        val imagesList: MutableList<String> = mutableListOf()

        // Firestore sorgusu
        val categoryRef = database.collection("Categories").document(categoryId)
        categoryRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Belge varsa resim URL'lerini al
                    val imageUrlList = documentSnapshot.get("imageUrl") as? List<String>
                    imageUrlList?.let {
                        imagesList.addAll(it)
                    }

                    imageList = imagesList

                    // İlk resmi varsayılan olarak göster
                    if (imageList.isNotEmpty()) {
                        loadImage(Uri.parse(imageList[currentImageIndex])) // String'ten Uri'ye dönüştürme
                    }
                } else {
                    // Belge yoksa hata durumu
                    Log.e("Firestore", "Document not found for categoryId: $categoryId")
                }
            }
            .addOnFailureListener { exception ->
                // Hata durumunda yapılacak işlemler
                Log.e("Firestore", "Error getting document", exception)
            }
    }

    private fun loadImage(imageUri: Uri) {
        Log.d("Firestore", "Loading image: $imageUri")
        Glide.with(requireContext())
            .load(imageUri)
            .into(binding.imageId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImageBinding.inflate(inflater, container, false)
        return binding.root
    }
}
