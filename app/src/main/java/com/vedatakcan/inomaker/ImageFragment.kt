package com.vedatakcan.inomaker

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatSeekBar
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
            navController.navigate(R.id.action_imageFragment_to_optionsFragment)
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

        if (categoryId != null) {
            loadImagesForCategory(categoryId)
            disableSeekBarInteraction(horizontalProgressBar)
        }
    }

    private fun disableSeekBarInteraction(seekBar: SeekBar) {
        seekBar.isEnabled = false  // Kullanıcının thumb'ı hareket ettirmesini engeller
        seekBar.isClickable = false // Kullanıcının thumb'ı tıklamasını engeller
       // seekBar.setOnTouchListener { _, _ -> true } // Kullanıcının thumb'ı sürüklemesini engeller
    }



    private fun updateProgressBar(progressBar: ProgressBar) {
        val progress = ((currentImageIndex + 1).toFloat() /imageList.size.toFloat())*100
        progressBar.progress = progress.toInt()
    }

    private fun showNextImage() {
        if (currentImageIndex < imageList.size - 1) {
            currentImageIndex++
            loadImage(imageList[currentImageIndex])

            // Görünürlüğü ayarla.
            binding.btnBack.visibility = View.VISIBLE
            binding.btnNext.visibility = if (currentImageIndex == imageList.size - 1) View.GONE else View.VISIBLE
        } else {
            // Son resimdeyiz, burada bir bildirim veya uyarma gösterebilirsiniz
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

            // İlk resimdeyiz, burada bir bildirim veya uyarma gösterebilirsiniz
            Toast.makeText(requireContext(), "İlk resim", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadImagesForCategory(categoryId: String) {
        database.collection("Categories")
            .document(categoryId)
            .collection("CategoryImages")
            .orderBy("order") // Resimlerin sırasını belirlemek için "order" alanına göre sıralama yapar
            .get()
            .addOnSuccessListener { documentSnapshot ->
                for (data in documentSnapshot) {
                    val imageUrl = data.get("imageUrl") as String
                    imageList.add(imageUrl)
                }
                if (imageList.isNotEmpty()) {
                    loadImage(imageList[currentImageIndex])
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents", exception)
            }
    }

    private fun loadImage(imageUrl: String) {
        Glide.with(requireContext())
            .load(imageUrl)
            .centerCrop()
            .into(binding.imageView)
    }
}
