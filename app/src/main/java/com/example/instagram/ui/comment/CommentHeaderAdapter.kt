package com.example.instagram.ui.comment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.databinding.ItemCommentHeaderBinding
import com.example.instagram.model.PostItem

/**
 * Created by Thanh Long Nguyen on 5/15/2021
 */
class CommentHeaderAdapter(
    private var post: PostItem?
) : RecyclerView.Adapter<CommentHeaderAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCommentHeaderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemCommentHeaderBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_comment_header,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        binding.post = post
        binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return 1
    }

    fun setPost(post: PostItem) {
        this.post = post
        notifyDataSetChanged()
    }
}