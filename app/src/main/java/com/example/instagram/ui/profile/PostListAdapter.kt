package com.example.instagram.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagram.databinding.ItemPostBinding
import com.example.instagram.model.PostItem

/**
 * Created by Thanh Long Nguyen on 4/16/2021
 */
class PostListAdapter(
    private val posts: List<PostItem>
) : RecyclerView.Adapter<PostListAdapter.PostViewHolder>() {

    companion object{
        private const val TAG = "PostListAdapter"
    }

    var onAvatarClick: ((PostItem) -> Unit)? = null
    var onOptionClick: ((PostItem) -> Unit)? = null
    var onLikeClick: ((Int, PostItem) -> Unit)? = null
    var onCommentClick: ((String) -> Unit)? = null
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
            if (postItem.likeCount > 1)
                "${postItem.likeCount} likes" else "${postItem.likeCount} like"
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
        binding.comment.setOnClickListener {
            onCommentClick?.invoke(postItem.postId)
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

    fun clickLike(position: Int) {
        val postItem = posts[position]
        if (postItem.isLiked){
            postItem.likeCount--
        } else {
            postItem.likeCount++
        }
        postItem.isLiked = !postItem.isLiked
        notifyItemChanged(position)
    }

    fun unlike(position: Int) {
        val postItem = posts[position]
        postItem.isLiked = !postItem.isLiked
        notifyItemChanged(position)
    }
}