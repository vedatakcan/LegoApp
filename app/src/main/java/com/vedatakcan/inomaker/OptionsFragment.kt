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
import androidx.appcompat.widget.SearchView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vedatakcan.inomaker.databinding.FragmentOptionsBinding
import com.vedatakcan.inomaker.model.CategoriesModel
import com.vedatakcan.inomaker.adapter.CategoriesAdapter
import com.vedatakcan.inomaker.repositories.CategoriesRepository


class OptionsFragment : Fragment(), MenuProvider {

    // Use the correct binding type for OptionsFragment
    private var _binding: FragmentOptionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: OptionsViewModel
    private lateinit var navController: NavController
    private lateinit var categoriesAdapter: CategoriesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the correct binding for OptionsFragment
        _binding = FragmentOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firestore = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()
        val repository = CategoriesRepository(firestore, storage)
        val factory = GeneralViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[OptionsViewModel::class.java]

        // Set up menu and navigation
        activity?.addMenuProvider(this, viewLifecycleOwner)
        navController = Navigation.findNavController(view)

        // Handle back press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navController.navigate(R.id.action_optionsFragment_to_startFragment)
        }

        // Set up adapter
        categoriesAdapter = CategoriesAdapter()
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = categoriesAdapter
        }

        // Fetch categories
        viewModel.fetchCategories()

        // Observe categories
        viewModel.categories.observe(viewLifecycleOwner) { list ->
            categoriesAdapter.submitData(list)
        }

        // Set up long click listener
        categoriesAdapter.setOnCategoryLongClickListener { category ->
            showDeleteConfirmationDialog(category)
            true
        }

        // Set up search view
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    val filtered = viewModel.filterCategories(it)
                    categoriesAdapter.submitData(filtered)
                }
                return true
            }
        })

        // Hide keyboard when focus is lost
        binding.searchView.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }
    }

    private fun showDeleteConfirmationDialog(category: CategoriesModel) {
        AlertDialog.Builder(requireContext())
            .setMessage("Kategoriyi silmek istediğinizden emin misiniz?")
            .setPositiveButton("Sil") { _, _ -> showPasswordDialog(category) }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showPasswordDialog(category: CategoriesModel) {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        AlertDialog.Builder(requireContext())
            .setTitle("Şifre Girişi")
            .setView(input)
            .setPositiveButton("Giriş") { _, _ ->
                if (input.text.toString().trim() == "1884") {
                    viewModel.deleteCategory(category) { success ->
                        if (success) viewModel.fetchCategories()
                    }
                } else {
                    showErrorDialog()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showErrorDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Hatalı Şifre")
            .setMessage("Girdiğiniz şifre hatalıdır.")
            .setPositiveButton("Tamam", null)
            .show()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_options, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        val destination = when (item.itemId) {
            R.id.addCategory -> R.id.action_optionsFragment_to_addCategoryFragment
            R.id.addImage -> R.id.action_optionsFragment_to_addImageFragment
            else -> null
        }

        destination?.let { showPasswordNavigationDialog(it) }
        return destination != null
    }

    private fun showPasswordNavigationDialog(destination: Int) {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        AlertDialog.Builder(requireContext())
            .setTitle("Parola")
            .setView(input)
            .setPositiveButton("Tamam") { _, _ ->
                if (input.text.toString().trim() == "1884") {
                    navController.navigate(destination)
                } else {
                    showErrorDialog()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


