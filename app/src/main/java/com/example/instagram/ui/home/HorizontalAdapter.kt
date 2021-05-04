package com.example.instagram.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.databinding.ItemStoriesBinding

/**
 * Created by Thanh Long Nguyen on 5/1/2021
 */
class HorizontalAdapter(
    private val storyListAdapter: StoryListAdapter
) : RecyclerView.Adapter<HorizontalAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemStoriesBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStoriesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        binding.storyRecyclerView.layoutManager =
            LinearLayoutManager(
                parent.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.storyRecyclerView.adapter = storyListAdapter
    }

    override fun getItemCount(): Int {
        return 1
    }

}