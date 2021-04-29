package com.example.instagram.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagram.databinding.ItemStoryBinding
import com.example.instagram.model.UserStory

/**
 * Created by Thanh Long Nguyen on 4/28/2021
 */
class StoryListAdapter(
    private val stories: List<UserStory>
) : RecyclerView.Adapter<StoryListAdapter.ViewHolder>() {

    var onItemClick: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = stories[position]
        Glide.with(holder.itemView.context)
            .load(story.stories[0].photo_url)
            .into(holder.binding.storyImage)
        holder.binding.username.text = story.username
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(position)
        }
    }

    override fun getItemCount(): Int {
        return stories.size
    }

    fun addAll(stories: List<UserStory>) {
        (this.stories as MutableList).clear()
        this.stories.addAll(stories)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root)
}