package com.example.instagram.ui.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagram.databinding.ItemGridPhotoBinding
import com.example.instagram.network.entity.Post

/**
 * Created by Thanh Long Nguyen on 4/27/2021
 */
class GridListAdapter(
    private val posts: List<Post>
) : RecyclerView.Adapter<GridListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemGridPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        Glide.with(holder.itemView.context)
            .load(post.photo_url)
            .into(holder.binding.image)
    }


    override fun getItemCount(): Int {
        return posts.size
    }

    class ViewHolder(val binding: ItemGridPhotoBinding) : RecyclerView.ViewHolder(binding.root)
}

