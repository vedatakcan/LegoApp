package com.vedatakcan.inomaker


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.vedatakcan.inomaker.databinding.FragmentImageBinding


class ImageFragment : Fragment() {

    private lateinit var binding: FragmentImageBinding
    private lateinit var navController: NavController
    private lateinit var database: FirebaseFirestore

    private var imageList: MutableList<String> = mutableListOf()
    private var currentImageIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        database = FirebaseFirestore.getInstance()

        val horizontalProgressBar = binding.horizontalProgressBar

        binding.btnBack.visibility = View.GONE

        binding.btnHome.setOnClickListener {
            navController.navigate(R.id.action_imageFragment_to_sectionsFragment)
        }

        binding.btnBack.setOnClickListener {
            showPreviousImage()
            updateProgressBar(horizontalProgressBar)
        }

        binding.btnNext.setOnClickListener {
            showNextImage()
            updateProgressBar(horizontalProgressBar)
        }

        val categoryId = arguments?.getString("categoryId")
        val sectionId = arguments?.getString("sectionId")

        if (categoryId != null && sectionId != null) {

            loadImagesForCategory(sectionId, categoryId)
            disableSeekBarInteraction(horizontalProgressBar)
        }
    }

    private fun disableSeekBarInteraction(seekBar: SeekBar) {
        seekBar.isEnabled = false
        seekBar.isClickable = false
    }

    private fun updateProgressBar(progressBar: ProgressBar) {
        val progress = ((currentImageIndex + 1).toFloat() / imageList.size.toFloat()) * 100
        progressBar.progress = progress.toInt()
    }

    private fun showNextImage() {
        if (currentImageIndex < imageList.size - 1) {
            currentImageIndex++
            loadImage(imageList[currentImageIndex])

            binding.btnBack.visibility = View.VISIBLE
            binding.btnNext.visibility =
                if (currentImageIndex == imageList.size - 1) View.GONE else View.VISIBLE
        } else {
            Toast.makeText(requireContext(), "Son resim", Toast.LENGTH_SHORT).show()
            binding.btnNext.visibility = View.GONE
        }
    }

    private fun showPreviousImage() {
        if (currentImageIndex > 0) {
            currentImageIndex--
            loadImage(imageList[currentImageIndex])

            binding.btnNext.visibility = View.VISIBLE
            binding.btnBack.visibility = if (currentImageIndex == 0) View.GONE else View.VISIBLE
        } else {
            binding.btnBack.visibility = View.GONE
            Toast.makeText(requireContext(), "İlk resim", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadImagesForCategory(sectionId: String, categoryId: String) {
        Log.d("Firestore", "loadImagesForCategory function started for categoryId: $categoryId")
        database.collection("Sections")
            .document(sectionId) // Kategoriye özgü document ID'sini burada kullanın
            .collection("Categories")
            .document(categoryId)
            .collection("CategoryImages")
            .orderBy("order")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                for (document in documentSnapshot) {
                    val documentId = document.id
                    val imageUrl = document.getString("imageUrl")
                    Log.d("Firestore", "Document ID: $documentId")

                    imageUrl?.let {
                        imageList.add(it)
                        Log.d("Firestore", "Image URL: $it for documentId: $documentId")
                    }
                }

                if (imageList.isNotEmpty()) {
                    loadImage(imageList[currentImageIndex])
                    Log.d(
                        "Firestore",
                        "Images loaded successfully, total images: ${imageList.size}"
                    )
                } else {
                    Log.d("Firestore", "No images found for categoryId: $categoryId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents for categoryId: $categoryId", exception)
            }
    }

    private fun loadImage(imageUrl: String) {
        Log.d("ImageFragment", "Loading image from URL: $imageUrl")
        Glide.with(requireContext())
            .load(imageUrl)
            .centerCrop()
            .into(binding.imageView)
    }
}

