package com.vedatakcan.inomaker


import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vedatakcan.inomaker.databinding.FragmentAddCategoryBinding
import com.vedatakcan.inomaker.repositories.CategoriesRepository


class AddCategoryFragment : Fragment() {

    private var selectedImage: Uri? = null
    private lateinit var binding: FragmentAddCategoryBinding
    private lateinit var navController: NavController
    private lateinit var viewModel: AddCategoryViewModel

    companion object {
        private const val GALLERY_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        val factory = GeneralViewModelFactory(CategoriesRepository(FirebaseFirestore.getInstance(), FirebaseStorage.getInstance()))
        viewModel = ViewModelProvider(this, factory)[AddCategoryViewModel::class.java]

        binding.btnHome.setOnClickListener {
            navController.navigate(R.id.action_addCategoryFragment_to_optionsFragment)
        }

        binding.btnAddCategory.setOnClickListener {
            addCategory()
        }

        binding.ivAddCategoryImage.setOnClickListener {
            uploadPhotos()
        }

        // LiveData gözlemi
        viewModel.categoryAdded.observe(viewLifecycleOwner) { success ->
            if (success) {
                showDialog()
            } else {
                Toast.makeText(requireContext(), "Kategori eklenemedi.", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.isImageValid.observe(viewLifecycleOwner) { isValid ->
            if (!isValid) {
                Toast.makeText(requireContext(), "Lütfen bir resim seçin.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun uploadPhotos() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImage = data.data
            binding.ivAddCategoryImage.setImageURI(selectedImage)
            viewModel.validateImage(selectedImage)
        }
    }

    private fun addCategory() {
        val categoryName = binding.tiCategoryName.text.toString()
        viewModel.validateCategoryName(categoryName)

        if (viewModel.isCategoryValid.value == true) {
            val isActive = binding.cbCategory.isChecked
            viewModel.addCategory(categoryName, isActive, selectedImage)
            binding.btnAddCategory.isEnabled = false // kategori başarıyla eklendi, buton pasif
        } else {
            Toast.makeText(requireContext(), "Lütfen kategori alanını doldurun", Toast.LENGTH_LONG).show()
        }
    }


    private fun showDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Kategori eklendi.")
        builder.setMessage("Kategoriye ait resimleri ekle.")

        builder.setPositiveButton("Ekle") { _, _ ->
            toUploadImage()
        }

        builder.setNegativeButton("Daha sonra") { dialog, _ ->
            dialog.cancel()
            navController.navigate(R.id.action_addCategoryFragment_to_optionsFragment)
        }

        builder.show()
    }

    private fun toUploadImage() {
        navController.navigate(R.id.action_addCategoryFragment_to_addImageFragment)
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }
}
