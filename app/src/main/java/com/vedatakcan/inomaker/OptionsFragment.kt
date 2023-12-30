package com.vedatakcan.inomaker

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
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
import androidx.activity.addCallback
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


    private lateinit var clickedSectionCategoriesList: ArrayList<CategoriesModel> // Tıklanan bölümün kategorilerini içerecek liste



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOptionsBinding.inflate(inflater, container, false)
        database = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { query ->
                    filterCategories(query)
                }
                return true
            }
        })

        binding.searchView.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.addMenuProvider(this, viewLifecycleOwner)
        navController = Navigation.findNavController(view)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navController.navigate(R.id.action_optionsFragment_to_startFragment)
        }

        val sectionId = arguments?.getString("sectionId")


        if (sectionId != null){
            loadCategoriesForSection(sectionId) // Seçilen bölüme ait kategorileri yükle
        }

        binding.btnHome.setOnClickListener {
            navController.navigate(R.id.action_optionsFragment_to_sectionsFragment)
        }

        categoriesAdapter = CategoriesAdapter()
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = categoriesAdapter
        }
    }

    private fun filterCategories(query: String) {
        val filteredList = clickedSectionCategoriesList.filter { category ->
            category.categoryName.contains(query, ignoreCase = true)
        }.toList()

        categoriesAdapter.submitData(filteredList)
    }


    private fun loadCategoriesForSection(sectionId: String) {
        database.collection("Sections").document(sectionId)
            .collection("Categories")
            .get()
            .addOnSuccessListener { documents ->
                val categoriesList = ArrayList<CategoriesModel>()

                for (document in documents) {
                    val categoryId = document.id
                    val categoryName = document.getString("categoryName") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val isActive = document.getBoolean("active") ?: false
                    val sectionIdd = document.getString("sectionId") ?: ""

                    val category = CategoriesModel(categoryId, categoryName, imageUrl, isActive, sectionId)
                    categoriesList.add(category)
                }

                // Elde edilen kategorileri RecyclerView'e göndermek için adapter'a gönder
                categoriesAdapter.submitData(categoriesList)
                // Tıklanan bölümün kategorilerini atama
                clickedSectionCategoriesList = categoriesList

                // Uzun basma olayını buraya taşıyın
                categoriesAdapter.setOnCategoryLongClickListener { category ->
                    showDeleteConfirmationDialog(category)
                    true
                }
            }
            .addOnFailureListener { exception ->
                // Hata durumunda hata mesajını göster
                Log.e("LoadCategories", "Kategoriler yüklenirken hata oluştu: ${exception.message}", exception)
            }
    }


    private fun showDeleteConfirmationDialog(category: CategoriesModel) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.apply {
            setMessage("Kategoriyi silmek istediğinizden emin misiniz?")
            setPositiveButton("Sil") { _, _ ->
                showPasswordDialog(category)
            }
            setNegativeButton("İptal") { dialog, _ ->
                dialog.dismiss()
            }
            create().show()
        }
    }

    private fun showPasswordDialog(category: CategoriesModel) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Şifre Girişi")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)

        builder.setPositiveButton("Giriş") { dialog, _ ->
            val enteredPassword = input.text.toString().trim()
            val correctPassword = "1984"

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


    private fun deleteCategoryImagesFromFirestore(sectionId: String, categoryId: String, categoryName: String?) {
        val categoryImagesRef = database.collection("Sections")
            .document(sectionId)
            .collection("Categories")
            .document(categoryId)
            .collection("CategoryImages")

        categoryImagesRef
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            // Resimler Firestore'dan silindi
                            Toast.makeText(requireContext(), "Kategori silindi bile", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener { exception ->
                            // Resimler Firestore'dan silinemedi
                            Toast.makeText(requireContext(), "Kategori silinmedi bile", Toast.LENGTH_LONG).show()
                        }
                }

                // Kategorinin kendi verilerini ve Storage'daki verilerini sil
                deleteCategoryImagesFromStorage(categoryName, categoryImageUrl = null)

                // CategoryImages koleksiyonunu sil
                database.collection("Sections")
                    .document(categoryId)
                    .collection("CategoryImages")
                    .document(categoryId)
                    .delete()
                    .addOnSuccessListener {
                        // Koleksiyon ve içindeki belgeler başarıyla silindi
                        Toast.makeText(requireContext(), "Kategori silindi", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { exception ->
                        // Koleksiyon silinemedi
                        Toast.makeText(requireContext(), "Kategori silinemedi", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { exception ->
                // Alt koleksiyon okunamadı
                Toast.makeText(requireContext(), "Alt koleksiyon okunmadı.", Toast.LENGTH_LONG).show()
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

            categoryImageUrl?.let { url ->
                val storageRefImage = storage.getReferenceFromUrl(url)
                storageRefImage.delete()
                    .addOnSuccessListener {
                        // Kategoriye ait kapak resmi başarıyla silindi
                    }
                    .addOnFailureListener {
                        // Kategoriye ait kapak resmi silinemedi
                    }}
        }
    }



    private fun deleteCategory(category: CategoriesModel) {
        category.categoryId?.let { categoryId ->
            val categoryRef = database.collection("Sections")
                .document(category.sectionId)
                .collection("Categories")
                .document(categoryId)

            categoryRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val categoryName = documentSnapshot.getString("categoryName")
                    val categoryImageUrl = documentSnapshot.getString("imageUrl")

                    categoryRef.delete()
                        .addOnSuccessListener {
                            deleteCategoryImagesFromFirestore(category.sectionId, categoryId, categoryName)
                            categoryImageUrl?.let {
                                deleteCategoryImagesFromStorage(categoryName, it)
                            }
                            Toast.makeText(requireContext(), "Kategori ve resimleri başarıyla silindi.", Toast.LENGTH_SHORT).show()
                            // Kategoriyi silme işlemi tamamlandıktan sonra kategorileri yeniden yükle
                            loadCategoriesForSection(category.sectionId)
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
            R.id.addSection -> {
                showPasswordDialog(R.id.action_optionsFragment_to_addSectionFragment)
                return true
            }
        }
        return false
    }

    private fun showPasswordDialog(destinations: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Parola")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)

        builder.setPositiveButton("Tamam") { dialog, _ ->
            val enteredPassword = input.text.toString().trim()
            val correctPassword = "1881"

            if (enteredPassword == correctPassword) {
                navController.navigate(destinations)
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


