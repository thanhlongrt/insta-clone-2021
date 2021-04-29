package com.example.instagram.ui.profile.user_post

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagram.databinding.ItemGridPhotoBinding
import com.example.instagram.firebase_model.Post

/**
 * Created by Thanh Long Nguyen on 4/14/2021
 */
class PostGridListAdapter(
    private val photosUrls: List<Post>
) : RecyclerView.Adapter<PostGridListAdapter.PhotoViewHolder>() {

    var onItemClicked: ((Int) -> Unit)? = null

    inner class PhotoViewHolder(val binding: ItemGridPhotoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding =
            ItemGridPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photosUrls[position]
        Glide.with(holder.itemView.context)
            .load(photo.url)
            .into(holder.binding.image)

        holder.itemView.setOnClickListener {

            onItemClicked?.invoke(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return photosUrls.size
    }

    fun addAll(urls: List<Post>?) {

        urls?.let {
            (photosUrls as MutableList).clear()
            photosUrls.addAll(it)
            notifyDataSetChanged()
        }

    }
}