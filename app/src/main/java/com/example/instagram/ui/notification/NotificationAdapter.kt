package com.example.instagram.ui.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagram.R
import com.example.instagram.databinding.ItemNotificationBinding
import com.example.instagram.network.entity.Notification

/**
 * Created by Thanh Long Nguyen on 5/8/2021
 */
class NotificationAdapter(
    private val notifications: MutableList<Notification>
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    var onClick: ((Int, Notification) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        val binding = holder.binding
        binding.notification = notification
        binding.executePendingBindings()
        binding.layout.setOnClickListener {
            onClick?.invoke(position, notification)
        }
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    fun addAll(new: List<Notification>) {
        notifications.clear()
        notifications.addAll(new)
        notifyDataSetChanged()
    }

    fun seen(position: Int) {
        if (!notifications[position].seen) {
            notifications[position].seen = true
            notifyItemChanged(position)
        }
    }
}