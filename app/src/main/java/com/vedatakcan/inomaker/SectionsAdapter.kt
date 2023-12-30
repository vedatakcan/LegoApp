package com.vedatakcan.inomaker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vedatakcan.inomaker.databinding.ItemSectionViewBinding


class SectionsAdapter : RecyclerView.Adapter<SectionsAdapter.SectionsViewHolder>() {

    private var sectionsList: MutableList<SectionsModel> = mutableListOf()
    private var onSectionClickListener: ((SectionsModel) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSectionViewBinding.inflate(inflater, parent, false)
        return SectionsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return sectionsList.size
    }

    override fun onBindViewHolder(holder: SectionsViewHolder, position: Int) {
        holder.bind(sectionsList[position])
    }

    inner class SectionsViewHolder(private val binding: ItemSectionViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(sectionsModel: SectionsModel) {
            binding.apply {
                sectionsName.text = sectionsModel.sectionName
                // Set other views based on SectionsModel properties
                // Glide veya başka bir kütüphane kullanarak resmi yükleme
                Glide.with(itemView.context)
                    .load(sectionsModel.sectionImageUrl)
                    .into(sectionImage)

                itemView.setOnClickListener {
                    val sectionId = sectionsModel.sectionId
                    val bundle = bundleOf("sectionId" to sectionId)
                    itemView.findNavController().navigate(R.id.action_sectionsFragment_to_optionsFragment, bundle)
                }

                /*
                itemView.setOnClickListener {
                    onSectionClickListener?.invoke(sectionsList[adapterPosition])
                }

                 */
            }
        }
    }

    fun submitData(list: List<SectionsModel>) {
        this.sectionsList.clear()
        this.sectionsList.addAll(list)
        notifyDataSetChanged()
    }

    fun setOnSectionClickListener(listener: (SectionsModel) -> Unit) {
        this.onSectionClickListener = listener
    }
}
