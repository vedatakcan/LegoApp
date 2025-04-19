package com.vedatakcan.inomaker

import android.content.Context
import android.graphics.drawable.Drawable
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.firestore.FirebaseFirestore
import com.vedatakcan.inomaker.databinding.FragmentImageBinding


class ImageFragment : Fragment() {

    private lateinit var binding: FragmentImageBinding
    private lateinit var viewModel: ImageViewModel
    private lateinit var navController: NavController

    private var imageList: List<String> = listOf()
    private var currentImageIndex = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        val firestore = FirebaseFirestore.getInstance()
        val repository = ImageRepository(firestore)
        val factory = ImageViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ImageViewModel::class.java]

        val categoryId = arguments?.getString("categoryId")
        categoryId?.let { viewModel.fetchImages(it) }

        viewModel.images.observe(viewLifecycleOwner) { images ->
            imageList = images
            if (imageList.isNotEmpty()) {
                // İlk resmi doğrudan yükle
                loadImage(imageList[0])

                // Diğerlerini önbelleğe al
                preloadRemainingImages(imageList)

                updateProgressBar(binding.horizontalProgressBar)
            }
        }

        disableSeekBarInteraction(binding.horizontalProgressBar)

        binding.btnHome.setOnClickListener {
            navController.navigate(R.id.action_imageFragment_to_optionsFragment)
        }

        binding.btnBack.setOnClickListener {
            showPreviousImage()
            updateProgressBar(binding.horizontalProgressBar)
        }

        binding.btnNext.setOnClickListener {
            showNextImage()
            updateProgressBar(binding.horizontalProgressBar)
        }

        binding.btnBack.visibility = View.GONE
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
            binding.btnNext.visibility = if (currentImageIndex == imageList.size - 1) View.GONE else View.VISIBLE
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
            Toast.makeText(requireContext(), "İlk resim", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadImage(imageUrl: String) {
        // ProgressBar'ı görünür yapıyoruz
        binding.imageLoadingProgress.visibility = View.VISIBLE

        Glide.with(requireContext())
            .load(imageUrl)
            .centerCrop()
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    // Yükleme tamamlandığında ProgressBar'ı gizliyoruz
                    binding.imageView.setImageDrawable(resource)
                    binding.imageLoadingProgress.visibility = View.GONE
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Görüntü yüklemesi temizlendiğinde yapılacaklar (opsiyonel)
                    binding.imageLoadingProgress.visibility = View.GONE
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    // Yükleme hatası durumunda ProgressBar'ı gizliyoruz
                    binding.imageLoadingProgress.visibility = View.GONE
                    Log.e("ImageFragment", "Image load failed")
                }
            })
    }


    private fun preloadRemainingImages(images: List<String>) {
        for (i in 1 until images.size) {
            Glide.with(requireContext())
                .load(images[i])
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .preload()
        }
    }
}
