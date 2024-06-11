package com.example.timewise.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.timewise.R
import com.example.timewise.databinding.ItemChunkBinding
import com.example.timewise.databinding.ItemPicturesBinding
import com.example.timewise.databinding.ItemTagsBinding
import com.example.timewise.model.ChunkModel

class PicturesAdapter(
    var list: ArrayList<String>,
    var context: Context
) :
    RecyclerView.Adapter<PicturesAdapter.VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_pictures, parent, false)
        return VH(view)
    }

    fun setExerciseList(newList: ArrayList<String>) {
        list = newList
        notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int) {
        val picture = list[position]
        Glide.with(holder.binding.ivPicture)
            .asBitmap()
            .load(Uri.parse(picture))
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    holder.binding.ivPicture.setImageBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })

        holder.binding.ivDelete.setOnClickListener {
            list.removeAt(position)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemPicturesBinding

        init {
            binding = ItemPicturesBinding.bind(itemView)
        }
    }
}