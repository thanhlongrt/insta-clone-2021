package com.example.instagram.ui.explore.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagram.databinding.ItemSearchResultBinding
import com.example.instagram.model.UserItem

/**
 * Created by Thanh Long Nguyen on 4/17/2021
 */
class SearchResultAdapter(
    private val users: MutableList<UserItem>
) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    var onClick: ((UserItem) -> Unit)? = null
    var onCancelClick: ((UserItem) -> Unit)? = null

    class ViewHolder(val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        Glide.with(holder.itemView.context)
            .load(user.avatarUrl)
            .into(holder.binding.avatar)
        holder.binding.username.text = user.username
        holder.binding.displayName.text = user.displayName
        holder.itemView.setOnClickListener {
            onClick?.invoke(user)
        }
        holder.binding.deleteButton.setOnClickListener {
            onCancelClick?.invoke(user)
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    fun addAll(newResult: List<UserItem>){
        this.users.clear()
        this.users.addAll(newResult)
        notifyDataSetChanged()
    }
}