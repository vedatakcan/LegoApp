package com.vedatakcan.inomaker


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.firestore.FirebaseFirestore
import com.vedatakcan.inomaker.databinding.FragmentAddCategoryBinding



class AddCategoryFragment : Fragment() {
    private lateinit var binding: FragmentAddCategoryBinding
    private lateinit var database: FirebaseFirestore
    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(context, "ADD category", Toast.LENGTH_LONG).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAddCategoryBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        database = FirebaseFirestore.getInstance()

        binding.btnAddCategory.setOnClickListener {
            addCategory()
            navController.navigate(R.id.action_addCategoryFragment_to_optionsFragment)
        }

        binding.ivAddCategoryImage.setOnClickListener {
            uploadPhotos()
        }


    }

    private fun uploadPhotos() {
        // Resim yükleme işlemleri burada gerçekleştirilir.
        // ...
    }

    private fun addCategory() {

        if (isValid()) {

            val categoryName = binding.tiCategoryName.text.toString()
            val isActive = binding.cbCategory.isChecked


            database.collection("Categories")
                .add(CategoriesModel(categoryName = categoryName, active = isActive))
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Kategori Eklendi",
                        Toast.LENGTH_LONG
                    ).show()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Kategori eklenemedi.", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(requireContext(), "Lütfen kategori alanını doldur", Toast.LENGTH_LONG).show()
        }
    }

    private fun isValid(): Boolean {
        val categoryName = binding.tiCategoryName.text.toString()
        return !categoryName.isNullOrEmpty() && categoryName.isNotBlank()
    }


    override fun onResume() {
        super.onResume()
        // Get the ActionBar
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        // Hide the ActionBar
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }


}


