package com.example.instagram.ui.profile.user_post

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagram.databinding.ItemPostBinding
import com.example.instagram.firebase_model.Like
import com.example.instagram.firebase_model.User
import com.example.instagram.model.PostItem

/**
 * Created by Thanh Long Nguyen on 4/16/2021
 */
class PostListAdapter(
    private val posts: List<PostItem>
) : RecyclerView.Adapter<PostListAdapter.PostViewHolder>() {

    var onAvatarClick: ((PostItem) -> Unit)? = null
    var onOptionClick: ((PostItem) -> Unit)? = null
    var onLikeClick: ((Int, PostItem) -> Unit)? = null
    var onCommentClick: ((PostItem) -> Unit)? = null
    var onSendClick: ((PostItem) -> Unit)? = null

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
        val postItem = posts[position]
        val binding = holder.binding
        val context = holder.itemView.context
        Glide.with(context)
            .load(postItem.avatarUrl)
            .into(binding.avatar)
        Glide.with(context)
            .load(postItem.photoUrl)
            .into(binding.photo)
        binding.upperUsername.text = postItem.userName
        binding.likeCount.text =
            if (postItem.likes.size > 1)
                "${postItem.likes.size} likes" else "${postItem.likes.size} like"
        binding.username.text = postItem.userName
        binding.caption.text = postItem.caption
        binding.dateCreated.text = postItem.date.toString()

        binding.options.setOnClickListener {
            onOptionClick?.invoke(postItem)
        }

        binding.like.isChecked = postItem.isLiked
        binding.like.setOnClickListener {
            it.animate().scaleX(0.75f).scaleY(0.75f).setDuration(0).withEndAction {
                it.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).withEndAction {
                    it.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)
                }
            }
            onLikeClick?.invoke(position, postItem)

        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    fun addAll(posts: List<PostItem>) {
        (this.posts as MutableList).clear()
        this.posts.addAll(posts)
        notifyDataSetChanged()
    }

    fun like(position: Int, uid: String) {
        posts[position].isLiked = !posts[position].isLiked
        (posts[position].likes as MutableList).add(Like(uid = uid))
        notifyItemChanged(position)
    }

    fun unlike(position: Int, uid: String) {
        posts[position].isLiked = !posts[position].isLiked
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            (posts[position].likes as MutableList).removeIf { it.uid == uid }
        }
        notifyItemChanged(position)
    }
}