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
import com.example.timewise.model.ChunkModel

class ChunksAdapter(
    var list: List<ChunkModel>,
    var context: Context,
    var checkFrom: String
) :
    RecyclerView.Adapter<ChunksAdapter.VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chunk, parent, false)
        return VH(view)
    }

    fun setChunkList(newList: List<ChunkModel>){
        list = newList
        notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int) {
        val chunkModel = list[position]
        when(chunkModel.activityName){
            "Exercise" -> {
                holder.binding.ivLogo.setImageResource(R.drawable.iv_exercise)
            }
            "Graduation" -> {
                holder.binding.ivLogo.setImageResource(R.drawable.iv_graduation)
            }
            "Productivity" -> {
                holder.binding.ivLogo.setImageResource(R.drawable.iv_productivity)
            }
            else -> {
                holder.binding.ivLogo.setImageResource(R.drawable.timewise_logo)
            }
        }
        holder.binding.tvTitle.text = "Title: "+chunkModel.title
        holder.binding.tvDate.text = chunkModel.date
        holder.binding.tvDuration.text = "Time: "+chunkModel.startTime+" - "+chunkModel.endTime
        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("data", chunkModel)
            if (checkFrom == "home"){
                Navigation.findNavController(it).navigate(R.id.action_navigation_home_to_createChunkFragment, bundle)
            }else{
                Navigation.findNavController(it).navigate(R.id.action_navigation_dashboard_to_createChunkFragment, bundle)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemChunkBinding

        init {
            binding = ItemChunkBinding.bind(itemView)
        }
    }
}