package com.example.instagram.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagram.databinding.ItemPostBinding
import com.example.instagram.databinding.ItemStoriesBinding
import com.example.instagram.firebase_model.Like
import com.example.instagram.model.PostItem
import com.example.instagram.model.StoryItem

/**
 * Created by Thanh Long Nguyen on 5/1/2021
 */
class FeedListAdapter(
    private val storyItems: List<StoryItem>,
    private val posts: List<PostItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_STORIES = 0
        private const val TYPE_POST = 1
    }

    var onStoryClick: ((Int) -> Unit)? = null
    var onAvatarClick: ((PostItem) -> Unit)? = null
    var onOptionClick: ((PostItem) -> Unit)? = null
    var onLikeClick: ((Int, PostItem) -> Unit)? = null
    var onCommentClick: ((PostItem) -> Unit)? = null
    var onSendClick: ((PostItem) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_STORIES -> {
                val binding = ItemStoriesBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
                return StoriesViewHolder(binding)
            }
            TYPE_POST -> {
                val binding = ItemPostBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return PostViewHolder(binding)
            }
            else -> {
                throw IllegalStateException("Unexpected view type")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_STORIES -> {
                val storyAdapter = StoryListAdapter(mutableListOf())
                storyAdapter.onItemClick = {
                    onStoryClick?.invoke(it)
                }
                val binding = (holder as StoriesViewHolder).binding
                binding.storyRecyclerView.apply {
                    layoutManager = LinearLayoutManager(
                        holder.itemView.context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    adapter = storyAdapter
                }
                storyAdapter.addAll(storyItems)
            }

            TYPE_POST -> {
                val postPosition = position - 1
                val postItem = posts[postPosition]
                val binding = (holder as PostViewHolder).binding
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
                    onLikeClick?.invoke(postPosition, postItem)

                }
            }
        }
    }

    override fun getItemCount(): Int {
        return posts.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_STORIES
        } else {
            TYPE_POST
        }
    }

    fun fetchStories(storyItems: List<StoryItem>) {
        (this.storyItems as MutableList).clear()
        this.storyItems.addAll(storyItems)
        notifyItemChanged(0)
    }

    fun fetchPosts(posts: List<PostItem>) {
        (this.posts as MutableList).clear()
        this.posts.addAll(posts)
        notifyItemRangeChanged(1, this.posts.size)
    }

    fun unlike(position: Int, uid: String) {
        posts[position].isLiked = !posts[position].isLiked
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            (posts[position].likes as MutableList).removeIf { it.uid == uid }
        }
        notifyItemChanged(position + 1)
    }

    fun like(position: Int, uid: String) {
        posts[position].isLiked = !posts[position].isLiked
        (posts[position].likes as MutableList).add(Like(uid = uid))
        notifyItemChanged(position + 1)
    }

    class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    class StoriesViewHolder(val binding: ItemStoriesBinding) : RecyclerView.ViewHolder(binding.root)
}