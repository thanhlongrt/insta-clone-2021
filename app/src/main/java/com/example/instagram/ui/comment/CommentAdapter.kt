package com.example.instagram.ui.comment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.databinding.ItemCommentBinding
import com.example.instagram.network.entity.Comment

/**
 * Created by Thanh Long Nguyen on 5/6/2021
 */
class CommentAdapter(
    private val comments: MutableList<Comment>
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = DataBindingUtil.inflate<ItemCommentBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_comment,
            parent, false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        val binding = holder.binding
        binding.comment = comment
        binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    fun addAll(list: List<Comment>) {
        comments.clear()
        comments.addAll(list)
        notifyDataSetChanged()
    }
}