package com.example.timewise.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.timewise.R
import com.example.timewise.databinding.ItemChunkBinding
import com.example.timewise.databinding.ItemTagsBinding
import com.example.timewise.model.ChunkModel

class TagsAdapter(
    var list: ArrayList<String>,
    var context: Context
) :
    RecyclerView.Adapter<TagsAdapter.VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tags, parent, false)
        return VH(view)
    }

    fun setExerciseList(newList: ArrayList<String>) {
        list = newList
        notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int) {
        val tag = list[position]
        holder.binding.tvTag.text = tag
        holder.binding.ivDelete.setOnClickListener {
            list.removeAt(position)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemTagsBinding

        init {
            binding = ItemTagsBinding.bind(itemView)
        }
    }
}