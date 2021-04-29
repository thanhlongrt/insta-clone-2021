package com.example.instagram.ui.explore.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagram.databinding.ItemSearchResultBinding
import com.example.instagram.firebase_model.User

/**
 * Created by Thanh Long Nguyen on 4/17/2021
 */
class SearchResultAdapter(
    private val users: List<User>
) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    var onClick: ((User) -> Unit)? = null
    var onCancelClick: ((User) -> Unit)? = null

    class ViewHolder(val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        Glide.with(holder.itemView.context)
            .load(user.profile_photo)
            .into(holder.binding.profileImage)
        holder.binding.username.text = user.username
        holder.binding.displayName.text = user.display_name
        holder.itemView.setOnClickListener {
            onClick?.invoke(user)
        }
        holder.binding.cancelButton.setOnClickListener {
            onCancelClick?.invoke(user)
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    fun addAll(users: List<User>){
        (this.users as MutableList).clear()
        this.users.addAll(users)
        notifyDataSetChanged()
    }
}