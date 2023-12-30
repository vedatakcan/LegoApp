package com.vedatakcan.inomaker

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import com.vedatakcan.inomaker.databinding.FragmentAddSectionBinding
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_AUTO
import java.util.UUID

class AddSectionFragment : Fragment() {

    private var selectedImage: Uri? = null
    private var selectedBitmap: Bitmap? = null

    private lateinit var storage: FirebaseStorage
    private lateinit var binding: FragmentAddSectionBinding
    private lateinit var database: FirebaseFirestore
    private lateinit var navController: NavController


    companion object {
        private const val GALLERY_REQUEST_CODE = 1
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddSectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        database = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        binding.btnHome.setOnClickListener {
            navController.navigate(R.id.action_addSectionFragment_to_sectionsFragment)
        }

        binding.btnAddSection.setOnClickListener {
            addSection()
        }

        binding.ivAddSectionImage.setOnClickListener {
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
            binding.ivAddSectionImage.setImageBitmap(selectedBitmap)
        }
    }

    private fun isValid(): Boolean{
        val  sectionName = binding.tiSectionName.text.toString()
        return sectionName.isNotEmpty() && sectionName.isNotBlank()
    }

    private fun addSection() {
       val addSection = binding.btnAddSection
        addSection.isEnabled = false // Butonu devre dışı bırak.

        if (isValid()){
            val sectionName = binding.tiSectionName.text.toString()
            val isActive = binding.cbSection.isChecked

            if (selectedImage == null){
                //Eğer resim seçilmediyse uyarı göster.
                Toast.makeText(requireContext(), "Lütfen bir resim seçin.", Toast.LENGTH_LONG).show()
                addSection.isEnabled = true // Butonu etkinleştir.
            } else {
                // Resmin adını değiştir.
                val uuid = UUID.randomUUID()
                val imageName ="${uuid}.jpg"

                // Resmi depolama alanında alanına yükleme işlemi
                uploadImageToFirebaseStore(imageName, sectionName, isActive)
            }
        }else{
            Toast.makeText(requireContext(), "Lütfen bölüm alanını doldurun", Toast.LENGTH_LONG).show()
            addSection.isEnabled = true // Geçerli olmayan durumda butonu tekrar etkinleştir
            Log.d("ButtonStatus", "Button status after validation: ${addSection.isEnabled}")
        }
    }

    private fun uploadImageToFirebaseStore(imageName: String, sectionName: String, isActive: Boolean) {
        selectedImage?.let { uri ->
            val ref = storage.reference.child("section_image/$sectionName")
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        saveSectionToFirestore(sectionName, isActive, url.toString() )
                    }
                }
        }
    }

    private fun saveSectionToFirestore(sectionName: String, isActive: Boolean, imageUrl: String) {
        database.collection("Sections")
            .add(
                SectionsModel(
                    sectionName = sectionName,
                    active = isActive,
                    sectionImageUrl = imageUrl
                )
            )
            .addOnSuccessListener {
                showDialog()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Section eklenemedi: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("FirestoreError", "Error adding section", e)
            }
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Bölüm eklendi.")
        builder.setMessage("Kategori eklemek ister misin?")

        builder.setPositiveButton("Ekle") {_,_ ->
            toUploadImage()
        }

        builder.setNegativeButton("Daha sonra") { dialog, _ ->
            dialog.cancel()
            navController.navigate(R.id.action_addSectionFragment_to_sectionsFragment)
        }
        builder.show()
    }

    private fun toUploadImage() {
        // Kategori ekleme sayfasına gidilecek.
        navController.navigate(R.id.action_addSectionFragment_to_addCategoryFragment)
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }
}