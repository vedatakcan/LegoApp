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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.firestore.FirebaseFirestore
import com.vedatakcan.inomaker.R
import com.vedatakcan.inomaker.databinding.FragmentImageBinding
import com.vedatakcan.inomaker.repositories.ImageRepository
import com.vedatakcan.inomaker.viewmodel.ImageViewModel
import com.vedatakcan.inomaker.viewmodel.ImageViewModelFactory


class ImageFragment : Fragment() {

    private lateinit var binding: FragmentImageBinding
    private lateinit var viewModel: ImageViewModel
    private lateinit var navController: NavController

    private var imageList: List<String> = emptyList()
    private var currentImageIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        setupViewModel()
        observeImages()

        val categoryId = arguments?.getString("categoryId")
        categoryId?.let { viewModel.fetchImages(it) }

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
        viewModel.images.observe(viewLifecycleOwner) { images ->
            imageList = images ?: emptyList()
            currentImageIndex = 0

            if (imageList.isNotEmpty()) {
                displayImage(currentImageIndex)
            } else {
                binding.imageView.setImageDrawable(null)
                updateNavigationButtons()
            }
        }
    }

    private fun displayImage(index: Int) {
        val imageUrl = imageList.getOrNull(index) ?: return

        binding.imageLoadingProgress.visibility = View.VISIBLE

        Glide.with(requireContext())
            .load(imageUrl)
            .centerCrop()
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
}
