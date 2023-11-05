package com.vedatakcan.inomaker

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.vedatakcan.inomaker.databinding.FragmentOptionsBinding


class OptionsFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentOptionsBinding
    private lateinit var navController: NavController

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

        return binding.root
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_options, menu)
    }



    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.addCategory -> {
                // Öğe 1'e tıklanınca yapılacak işlem
                navController.navigate(R.id.action_optionsFragment_to_categoryAndImageAddFragment)
                return true
            }
            R.id.addImage -> {
                // Öğe 2'ye tıklanınca yapılacak işlem
                return true
            }
        }
        return false
    }


}
