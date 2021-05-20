package com.example.instagram.ui.create.choose_media

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagram.databinding.ItemGridPhotoBinding

/**
 * Created by Thanh Long Nguyen on 5/18/2021
 */
class DeviceImageAdapter(
    private val mediaList: MutableList<GalleryMedia>
) : RecyclerView.Adapter<DeviceImageAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemGridPhotoBinding) : RecyclerView.ViewHolder(binding.root)

    var onItemSelected: ((GalleryMedia, Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGridPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val media = mediaList[position]
        Glide.with(holder.itemView.context)
            .load(media.uri)
            .into(holder.binding.image)
        holder.binding.selected.visibility = if (media.isSelected) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener {
            onItemSelected?.invoke(media, position)
        }
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }

    fun addAll(list: List<GalleryMedia>) {
        mediaList.clear()
        mediaList.addAll(list)
        notifyDataSetChanged()
    }
}