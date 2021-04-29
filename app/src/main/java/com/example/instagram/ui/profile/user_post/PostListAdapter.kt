package com.example.instagram.ui.profile.user_post

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagram.databinding.ItemPostBinding
import com.example.instagram.firebase_model.Post
import com.example.instagram.firebase_model.User

/**
 * Created by Thanh Long Nguyen on 4/16/2021
 */
class PostListAdapter(
    private val user: User,
    private val posts: List<Post>
) : RecyclerView.Adapter<PostListAdapter.PostViewHolder>() {

    var onOptionClick: ((Post) -> Unit)? = null


    class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        val binding = holder.binding
        val context = holder.itemView.context
        Glide.with(context)
            .load(user.profile_photo)
            .into(binding.profileImage)
        Glide.with(context)
            .load(post.url)
            .into(binding.photo)
        binding.upperUsername.text = user.username
        binding.likeAmount.text = "${post.likes} likes"
        binding.username.text = user.username
        binding.caption.text = post.caption
        binding.dateCreated.text = post.date_created.toString()

        binding.options.setOnClickListener {
            onOptionClick?.invoke(post)
        }

    }

    override fun getItemCount(): Int {
        return posts.size
    }

    fun addAll(posts: List<Post>){
        (this.posts as MutableList).clear()
        this.posts.addAll(posts)
        notifyDataSetChanged()
    }
}