package com.vedatakcan.inomaker

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
import android.widget.EditText
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vedatakcan.inomaker.databinding.FragmentSectionsBinding


class SectionsFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentSectionsBinding
    private lateinit var navController: NavController
    private lateinit var database: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var sectionsAdapter: SectionsAdapter
    private val sectionList = ArrayList<SectionsModel>()



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.addMenuProvider(this, viewLifecycleOwner)
        navController = Navigation.findNavController(view)


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            navController.navigate(R.id.action_sectionsFragment_to_startFragment)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSectionsBinding.inflate(inflater, container, false)
        database = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
       // sectionsAdapter = SectionsAdapter()

        sectionsAdapter = SectionsAdapter()
        binding.recyclerViewSections.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = sectionsAdapter
        }

        getSections()


        return binding.root
    }



    private fun getSections() {
        sectionList.clear()
        database.collection("Sections")
            .get()
            .addOnCompleteListener { response ->
                if (response.isSuccessful){
                    sectionList.clear()
                    for (data in response.result){
                        try {
                            sectionList.add(
                                SectionsModel(
                                    sectionId = data.id,
                                    sectionName = data.get("sectionName") as String,
                                    active = data.get("active") as Boolean,
                                    sectionImageUrl = data.get("sectionImageUrl") as? String ?: ""

                                )
                            )
                        }catch (e: Exception){
                            Log.e("SectionModel", "Error: ${e.message}")
                        }
                    }
                    sectionsAdapter.submitData(sectionList)
                }
            }
    }





    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_sections, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId){
            R.id.addSection -> {
                showPasswordDialog()
                return true
            }
        }
        return false
    }

    private fun showPasswordDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Parola")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)

        builder.setPositiveButton("Tamam") { dialog, _ ->
            val enteredPassword = input.text.toString().trim()
            val correctPassword = "1881"

            if (enteredPassword == correctPassword) {
                navController.navigate(R.id.action_sectionsFragment_to_addSectionFragment)
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