package com.vedatakcan.inomaker.view

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.firestore.FirebaseFirestore
import com.vedatakcan.inomaker.R
import com.vedatakcan.inomaker.databinding.FragmentImageBinding
import com.vedatakcan.inomaker.repositories.ImageRepository
import com.vedatakcan.inomaker.viewmodel.ImageViewModel
import com.vedatakcan.inomaker.viewmodel.ImageViewModelFactory
import kotlinx.coroutines.launch


class ImageFragment : Fragment() {

    private var _binding: FragmentImageBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ImageViewModel
    private lateinit var navController: NavController

    private var imageList: List<String> = emptyList()
    private var currentImageIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        setupViewModel()

        val categoryId = arguments?.getString("categoryId")
        categoryId?.let { viewModel.fetchImages(it) }

        observeImages()
        disableSeekBar(binding.horizontalProgressBar)
        setupButtonListeners()
    }

    private fun setupViewModel() {
        val firestore = FirebaseFirestore.getInstance()
        val repository = ImageRepository(firestore)
        val factory = ImageViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ImageViewModel::class.java]
    }

    private fun observeImages() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.images.collect { images ->
                    imageList = images
                    if (imageList.isNotEmpty()) {
                        currentImageIndex = 0
                        displayImage(currentImageIndex)

                        // İlk başta tüm resimleri preload et
                        preloadAllImages()
                    } else {
                        binding.imageView.setImageDrawable(null)
                        updateNavigationButtons()
                    }
                }
            }
        }
    }

    private fun preloadAllImages() {
        // Tüm görselleri arka planda yükleyelim
        imageList.forEach { imageUrl ->
            Glide.with(requireContext())
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Disk cache kullan
                .skipMemoryCache(true)  // Bellek cache'ini atla, sadece disk cache kullan
                .override(500, 300) // Küçük boyutlarda yükle
                .preload() // Arka planda yükleme
        }
    }

    private fun displayImage(index: Int) {
        val imageUrl = imageList.getOrNull(index) ?: return

        binding.imageLoadingProgress.visibility = View.VISIBLE

        // Glide ile görseli yükleyip hemen ekranda gösteriyoruz
        Glide.with(requireContext())
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Disk cache kullanarak hızlı yükleme
            .skipMemoryCache(true)  // Bellek cache'i atlıyoruz
            .centerCrop()
            .override(500, 300) // 📏 Genişlik: 500px, Yükseklik: 300px
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    binding.imageView.setImageDrawable(resource)
                    binding.imageLoadingProgress.visibility = View.GONE
                    updateProgressBar()
                    updateNavigationButtons()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    binding.imageLoadingProgress.visibility = View.GONE
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    binding.imageLoadingProgress.visibility = View.GONE
                    Toast.makeText(requireContext(), "Resim yüklenemedi", Toast.LENGTH_SHORT).show()
                    Log.e("ImageFragment", "Image yükleme hatası")
                }
            })
    }

    private fun setupButtonListeners() {
        binding.btnHome.setOnClickListener {
            navController.navigate(R.id.action_imageFragment_to_optionsFragment)
        }

        binding.btnNext.setOnClickListener {
            if (currentImageIndex < imageList.size - 1) {
                currentImageIndex++
                displayImage(currentImageIndex)
            }
        }

        binding.btnBack.setOnClickListener {
            if (currentImageIndex > 0) {
                currentImageIndex--
                displayImage(currentImageIndex)
            }
        }
    }

    private fun updateNavigationButtons() {
        binding.btnBack.visibility = if (currentImageIndex > 0) View.VISIBLE else View.GONE
        binding.btnNext.visibility = if (currentImageIndex < imageList.size - 1) View.VISIBLE else View.GONE
    }

    private fun updateProgressBar() {
        val progress = ((currentImageIndex + 1).toFloat() / imageList.size.toFloat()) * 100
        binding.horizontalProgressBar.progress = progress.toInt()
    }

    private fun disableSeekBar(seekBar: SeekBar) {
        seekBar.isEnabled = false
        seekBar.isClickable = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


