package com.example.instagram.ui.comment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        val binding = holder.binding

        Glide.with(holder.itemView.context)
            .load(comment.avatar)
            .into(binding.avatar)

        binding.username.text = comment.username
        binding.content.text = comment.content
        binding.date.text = comment.date_created.toString()
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    fun addAll(list: List<Comment>){
        comments.clear()
        comments.addAll(list)
        notifyDataSetChanged()
    }
}