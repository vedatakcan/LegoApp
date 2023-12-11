package com.vedatakcan.inomaker

import android.content.Context
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vedatakcan.inomaker.databinding.FragmentOptionsBinding


class OptionsFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentOptionsBinding
    private lateinit var navController: NavController
    private lateinit var database: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var categoriesAdapter: CategoriesAdapter
    private val categoriesList = ArrayList<CategoriesModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.addMenuProvider(this, viewLifecycleOwner)
        navController = Navigation.findNavController(view)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOptionsBinding.inflate(inflater, container, false)
        database = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        categoriesAdapter = CategoriesAdapter()

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = categoriesAdapter
        }


        // SearchView için listener ekleme
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Arama butonuna basıldığında tetiklenir (kullanmadığınızda pas geçebilirsiniz)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Metin her değiştiğinde tetiklenir, burada arama işlemini yapabilirsiniz
                newText?.let { query ->
                    filterCategories(query)
                }
                return true
            }
        })
        // EditText'in odak değişikliğini dinleyerek klavyeyi kapatma
        binding.searchView.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        getCategories()
        return binding.root
    }



    private fun filterCategories(query: String) {
        val filteredList = categoriesList.filter { category ->
            category.categoryName.contains(query, ignoreCase = true)
        }.toList() // ArrayList'i List'e dönüştür

        categoriesAdapter.submitData(filteredList)
    }


    private fun getCategories() {
        categoriesList.clear()
        database.collection("Categories")
            .whereEqualTo("active", true)
            .get()
            .addOnCompleteListener { response ->
                if(response.isSuccessful){
                    categoriesList.clear()
                    for (data in response.result){
                        categoriesList.add(
                            CategoriesModel(
                                categoryId = data.id,
                                categoryName = data.get("categoryName") as String,
                                active = data.get("active") as Boolean,
                                imageUrl = data.get("imageUrl") as String
                            )
                        )
                    }
                    categoriesAdapter.submitData(categoriesList)
                }
            }

        categoriesAdapter.setOnCategoryLongClickListener { category ->
            showDeleteConfirmationDialog(category)
            true
        }
    }

    private fun showDeleteConfirmationDialog(category: CategoriesModel) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.apply {
            setMessage("Kategoriyi silmek istediğinizden emin misiniz?")
            setPositiveButton("Sil") { _, _ ->
                showPasswordDialogg(category)
            }
            setNegativeButton("İptal") { dialog, _ ->
                dialog.dismiss()
            }
            create().show()
        }
    }

    private fun showPasswordDialogg(category: CategoriesModel) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Şifre Girişi")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)

        builder.setPositiveButton("Giriş") { dialog, _ ->
            val enteredPassword = input.text.toString().trim()
            val correctPassword = "1881"

            if (enteredPassword == correctPassword) {
                deleteCategory(category)
            } else {
                showErrorDialog()
            }
        }

        builder.setNegativeButton("İptal") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun deleteCategoryImagesFromFirestore(categoryId: String, categoryName: String?) {
        val categoryImagesRef = database.collection("Categories").document(categoryId)
            .collection("CategoryImages")

        categoryImagesRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            // Resimler Firestore'dan silindi
                        }
                        .addOnFailureListener { exception ->
                            // Resimler Firestore'dan silinemedi
                        }
                }

                // Firestore'daki resimler silindikten sonra Firebase Storage'daki resimleri de sil
                deleteCategoryImagesFromStorage(categoryName, categoryImageUrl = null)
            }
            .addOnFailureListener { exception ->
                // Alt koleksiyon okunamadı
            }
    }

    private fun deleteCategoryImagesFromStorage(categoryName: String?, categoryImageUrl: String?) {
        categoryName?.let { name ->
            val storageRef = storage.reference.child("category_images/$name")
            storageRef.listAll()
                .addOnSuccessListener { listResult ->
                    for (item in listResult.items) {
                        item.delete()
                            .addOnSuccessListener {
                                // Resimler Storage'dan silindi
                            }
                            .addOnFailureListener {
                                // Resimler Storage'dan silinemedi
                            }
                    }
                }
                .addOnFailureListener {
                    // Resimler bulunamadı veya silinemedi
                }

            // Kategoriye ait kapak resminin silinmesi
            categoryImageUrl?.let { url ->
                val storageRef = storage.getReferenceFromUrl(url)
                storageRef.delete()
                    .addOnSuccessListener {
                        // Kategoriye ait kapak resmi başarıyla silindi
                    }
                    .addOnFailureListener {
                        // Kategoriye ait kapak resmi silinemedi
                    }
            }
        }
    }


    private fun deleteCategory(category: CategoriesModel) {
        category.categoryId?.let { categoryId ->
            val categoryRef = database.collection("Categories").document(categoryId)

            categoryRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val categoryName = documentSnapshot.getString("categoryName")
                    val categoryImageUrl = documentSnapshot.getString("imageUrl")

                    categoryRef.delete()
                        .addOnSuccessListener {
                            deleteCategoryImagesFromFirestore(categoryId, categoryName)
                            categoryImageUrl?.let {
                                deleteCategoryImagesFromStorage(categoryName, it)
                            }
                            Toast.makeText(requireContext(), "Kategori ve resimleri başarıyla silindi.", Toast.LENGTH_SHORT).show()
                            getCategories() // Kategorileri yeniden yükleyerek güncellemeyi sağlar
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Kategori silinemedi.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Kategori bulunamadı.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }




    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_options, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.addCategory -> {
                showPasswordDialog(R.id.action_optionsFragment_to_addCategoryFragment)
                return true
            }
            R.id.addImage -> {
                showPasswordDialog(R.id.action_optionsFragment_to_addImageFragment)
                return true
            }
        }
        return false
    }

    private fun showPasswordDialog(destination: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Parola")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)

        builder.setPositiveButton("Tamam") { dialog, _ ->
            val enteredPassword = input.text.toString().trim()
            val correctPassword = "1881"

            if (enteredPassword == correctPassword) {
                navController.navigate(destination)
            } else {
                showErrorDialog()
            }
        }

        builder.setNegativeButton("İptal") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun showErrorDialog() {
        val errorBuilder = AlertDialog.Builder(requireContext())
        errorBuilder.setTitle("Hatalı Şifre")
        errorBuilder.setMessage("Girdiğiniz şifre hatalıdır. Lütfen tekrar deneyin.")
        errorBuilder.setPositiveButton("Tamam") { dialog, which ->
            dialog.dismiss()
        }
        errorBuilder.show()
    }
}