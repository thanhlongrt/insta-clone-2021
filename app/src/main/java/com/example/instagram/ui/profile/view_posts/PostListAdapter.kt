package com.example.instagram.ui.profile.view_posts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.databinding.ItemPostBinding
import com.example.instagram.model.PostItem
import com.google.android.exoplayer2.ui.PlayerView

/**
 * Created by Thanh Long Nguyen on 4/16/2021
 */
class PostListAdapter(
    private val posts: MutableList<PostItem>
) : RecyclerView.Adapter<PostListAdapter.PhotoPostViewHolder>() {

    companion object {
        private const val TAG = "PostListAdapter"
    }

    var onAvatarClick: ((PostItem) -> Unit)? = null
    var onOptionClick: ((PostItem) -> Unit)? = null
    var onLikeClick: ((Int, PostItem) -> Unit)? = null
    var onCommentClick: ((String) -> Unit)? = null
    var onSendClick: ((PostItem) -> Unit)? = null

    var onViewAttachToWindow: ((PlayerView, String) -> Unit)? = null
    var onViewDetachedFromWindow: ((PlayerView, String) -> Unit)? = null

    class PhotoPostViewHolder(val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoPostViewHolder {
        val binding = DataBindingUtil.inflate<ItemPostBinding>(
            LayoutInflater.from(parent.context), R.layout.item_post, parent, false
        )
        return PhotoPostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoPostViewHolder, position: Int) {
        val binding = holder.binding
        val postItem = posts[position]
        binding.post = postItem
        binding.executePendingBindings()
        if (postItem.isVideo) {
            binding.root.addOnAttachStateChangeListener(object :
                View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View?) {
                    onViewAttachToWindow?.invoke(binding.playerView, postItem.videoUrl)
                }

                override fun onViewDetachedFromWindow(v: View?) {
                    onViewDetachedFromWindow?.invoke(binding.playerView, postItem.videoUrl)
                }
            })
        }
        binding.options.setOnClickListener {
            onOptionClick?.invoke(postItem)
        }
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
        this.posts.clear()
        this.posts.addAll(posts)
        notifyDataSetChanged()
    }

    fun onLikeClick(position: Int) {
        val postItem = posts[position]
        if (postItem.isLiked) {
            postItem.likeCount--
        } else {
            postItem.likeCount++
        }
        postItem.isLiked = !postItem.isLiked
        notifyItemChanged(position)
    }
}