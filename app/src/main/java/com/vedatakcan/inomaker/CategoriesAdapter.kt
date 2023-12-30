package com.vedatakcan.inomaker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vedatakcan.inomaker.databinding.ItemViewBinding

class CategoriesAdapter : RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder>() {

    private var categoriesList: MutableList<CategoriesModel> = mutableListOf()

    private lateinit var navController: NavController
    private var onCategoryLongClickListener: ((CategoriesModel) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemViewBinding.inflate(inflater, parent, false)

        navController = Navigation.findNavController(parent)
        return CategoriesViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return categoriesList.size
    }

    override fun onBindViewHolder(holder: CategoriesViewHolder, position: Int) {
        holder.bind(categoriesList[position])
    }

    inner class CategoriesViewHolder(private val binding: ItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(categoriesModel: CategoriesModel) {
            binding.apply {
                categoriesName.text = categoriesModel.categoryName
                Glide.with(itemView.context)
                    .load(categoriesModel.imageUrl)
                    .into(binding.imageClick)

                itemView.setOnClickListener {

                    val categoryId = categoriesModel.categoryId
                    val sectionId = categoriesModel.sectionId
                    val bundle = bundleOf(
                        "categoryId" to categoryId,
                        "sectionId" to sectionId
                    )
                    itemView.findNavController().navigate(R.id.action_optionsFragment_to_imageFragment, bundle)

                }

                itemView.setOnLongClickListener {
                    onCategoryLongClickListener?.invoke(categoriesList[adapterPosition])
                    true
                }
            }
        }
    }

    fun submitData(list: List<CategoriesModel>) {
        this.categoriesList.clear()
        this.categoriesList.addAll(list)
        notifyDataSetChanged()
    }

    fun setOnCategoryLongClickListener(listener: (CategoriesModel) -> Unit) {
        this.onCategoryLongClickListener = listener
    }
}





