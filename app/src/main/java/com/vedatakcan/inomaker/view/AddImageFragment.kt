package com.vedatakcan.inomaker.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vedatakcan.inomaker.R
import com.vedatakcan.inomaker.databinding.FragmentAddImageBinding
import com.vedatakcan.inomaker.adapter.ImageAdapter
import com.vedatakcan.inomaker.repositories.AddImageRepository
import com.vedatakcan.inomaker.viewmodel.AddImageViewModel
import com.vedatakcan.inomaker.viewmodel.AddImageViewModelFactory


class AddImageFragment : Fragment() {

    private lateinit var binding: FragmentAddImageBinding
    private lateinit var viewModel: AddImageViewModel
    private lateinit var imageAdapter: ImageAdapter
    private var selectedImageList = mutableListOf<Uri>()
    private var selectedCategoryName: String? = null
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddImageBinding.inflate(inflater, container, false)

        // ViewModel'ı ViewModelProvider ile başlatıyoruz.
        val repository = AddImageRepository(FirebaseStorage.getInstance(), FirebaseFirestore.getInstance())
        val factory = AddImageViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(AddImageViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
        imageAdapter = ImageAdapter(selectedImageList)
        binding.imRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.imRecyclerView.adapter = imageAdapter

        // Kategorileri çekiyoruz.
        viewModel.fetchCategories()

        // Kategorileri gözlemliyoruz ve spinner'ı güncelliyoruz.
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerClick.adapter = adapter
        }

        // Spinner'dan kategori seçimini izliyoruz.
        binding.spinnerClick.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategoryName = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Resim seçme butonuna tıklama işlemi.
        binding.chooseImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            startActivityForResult(intent, 1)
        }

        // Resim ekleme butonuna tıklama işlemi.
        binding.btnAddImage.setOnClickListener {
            selectedCategoryName?.let { category ->
                // ViewModel aracılığıyla resimleri yükleme işlemi.
                viewModel.uploadImages(category, selectedImageList)
            }
        }

        // Yükleme tamamlandığında başarılı mesajı göster.
        viewModel.uploadComplete.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "Resimler başarıyla kaydedildi", Toast.LENGTH_SHORT).show()
                navController.navigate(R.id.action_addImageFragment_to_optionsFragment)
            }
        }

        // Hata mesajlarını göster.
        viewModel.error.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), "Hata: $it", Toast.LENGTH_SHORT).show()
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            if (data.clipData != null) {
                for (i in 0 until data.clipData!!.itemCount) {
                    selectedImageList.add(data.clipData!!.getItemAt(i).uri)
                }
            } else if (data.data != null) {
                selectedImageList.add(data.data!!)
            }
            imageAdapter.notifyDataSetChanged()
            binding.imRecyclerView.visibility = View.VISIBLE
        }
    }
}
