package com.vedatakcan.inomaker

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.vedatakcan.inomaker.databinding.FragmentOptionsBinding


class OptionsFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentOptionsBinding
    private lateinit var navController: NavController
    private lateinit var database: FirebaseFirestore
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
        categoriesAdapter = CategoriesAdapter()

        binding.recyclerView.apply {
            binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)
            binding.recyclerView.adapter = categoriesAdapter
        }


        getCategories()
        return binding.root
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

    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_options, menu)
    }


    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.addCategory -> {
                // Öğe 1'e tıklanınca yapılacak işlem
                showPasswordDialog(R.id.action_optionsFragment_to_addCategoryFragment)
                return true
            }
            R.id.addImage -> {
                // Öğe 2'ye tıklanınca yapılacak işlem
                showPasswordDialog(R.id.action_optionsFragment_to_addImageFragment)
                return true
            }
        }
        return false
    }

    private fun showPasswordDialog(destination: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Şifre Girişi")

        // Şifre girişi için bir EditText alanı ekleyin
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD // Parolanın gizli olmasını sağlar
        builder.setView(input)

        builder.setPositiveButton("Giriş") { dialog, _ ->
            val enteredPassword = input.text.toString().trim()
            val correctPassword = "1881" // Doğru şifreyi burada tanımlayın

            if (enteredPassword == correctPassword) {
                // Şifre doğru, istenilen sayfaya yönlendirme yapabilirsiniz
                navController.navigate(destination)
            } else {
                // Hatalı şifre girişi hakkında bir uyarı gösterin
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
        errorBuilder.setPositiveButton("Tamam", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        errorBuilder.show()
    }



}
