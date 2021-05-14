package com.example.instagram.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.databinding.ItemGridPhotoBinding
import com.example.instagram.model.PostItem

/**
 * Created by Thanh Long Nguyen on 4/14/2021
 */
class PostGridListAdapter(
    private val photosUrls: MutableList<PostItem>
) : RecyclerView.Adapter<PostGridListAdapter.PhotoViewHolder>() {

    var onItemClicked: ((Int) -> Unit)? = null

    inner class PhotoViewHolder(val binding: ItemGridPhotoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding =
            DataBindingUtil.inflate<ItemGridPhotoBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_grid_photo,
                parent,
                false
            )
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val postItem = photosUrls[position]
        holder.binding.post = postItem
        holder.binding.executePendingBindings()
        holder.itemView.setOnClickListener {
            onItemClicked?.invoke(position)
        }
    }

    override fun getItemCount(): Int {
        return photosUrls.size
    }

    fun addAll(urls: List<PostItem>?) {
        urls?.let {
            photosUrls.clear()
            photosUrls.addAll(it)
            notifyDataSetChanged()
        }

    }
}