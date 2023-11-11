package com.vedatakcan.inomaker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vedatakcan.inomaker.databinding.FragmentAddImageBinding


class AddImageFragment : Fragment() {

    private lateinit var binding: FragmentAddImageBinding
    private lateinit var navController: NavController
    private lateinit var database: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        navController = Navigation.findNavController(view)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAddImageBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }


}