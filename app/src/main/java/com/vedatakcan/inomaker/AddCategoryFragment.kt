package com.vedatakcan.inomaker


import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vedatakcan.inomaker.databinding.FragmentAddCategoryBinding
import java.util.UUID


class AddCategoryFragment : Fragment() {

    var selectedImage: Uri? = null
    var selectedBitmap: Bitmap? = null

    private lateinit var storage: FirebaseStorage
    private lateinit var binding: FragmentAddCategoryBinding
    private lateinit var database: FirebaseFirestore
    private lateinit var navController: NavController

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

        navController = Navigation.findNavController(view)
        database = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        binding.btnAddCategory.setOnClickListener {
            addCategory()
        }

        binding.ivAddCategoryImage.setOnClickListener {
            uploadPhotos()
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
            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedImage)
            binding.ivAddCategoryImage.setImageBitmap(selectedBitmap)
        }
    }

    private fun addCategory() {
        if (isValid()) {
            // Depo işlemleri
            val uuid = UUID.randomUUID()
            val gorselIsmi = "${uuid}.jpg"

            uploadImageToFirebaseStorage()
        } else {
            Toast.makeText(requireContext(), "Lütfen kategori alanını doldurun", Toast.LENGTH_LONG).show()
        }
    }

    private fun isValid(): Boolean {
        val categoryName = binding.tiCategoryName.text.toString()
        return !categoryName.isNullOrEmpty() && categoryName.isNotBlank()
    }

    private fun uploadImageToFirebaseStorage() {
        selectedImage?.let { uri ->
            val ref = storage.reference.child("category_images/${UUID.randomUUID()}.jpg")
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        saveCategoryToFirestore(url.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Resim yüklenirken bir hata oluştu.", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun saveCategoryToFirestore(imageUrl: String) {
        val categoryName = binding.tiCategoryName.text.toString()
        val isActive = binding.cbCategory.isChecked

        database.collection("Categories")
            .add(CategoriesModel(categoryName = categoryName, active = isActive, imageUrl = imageUrl))
            .addOnSuccessListener {
                showDialog()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Kategori eklenemedi.", Toast.LENGTH_LONG).show()
            }
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Kategori eklendi.")
        builder.setMessage("Kategoriye ait resimleri ekle.")

        builder.setPositiveButton("Ekle", DialogInterface.OnClickListener {dialog, which ->
            toUploadImage()
        })

        builder.setNegativeButton("Daha sonra",DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
            navController.navigate(R.id.action_addCategoryFragment_to_optionsFragment)
        })

        builder.show()
    }

    private fun toUploadImage() {
       // Resim ekleme sayfasına gidilecek.
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
