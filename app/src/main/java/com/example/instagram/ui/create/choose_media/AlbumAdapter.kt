package com.example.instagram.ui.create.choose_media

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.databinding.ItemAlbumBinding

/**
 * Created by Thanh Long Nguyen on 5/19/2021
 */
class AlbumAdapter(
    private val albums: MutableList<Album>
) : RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root)

    var onItemClick: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAlbumBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val album = albums[position]
        holder.binding.albumTitle.text = album.title
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(album.id)
        }
    }

    override fun getItemCount(): Int {
        return albums.size
    }

    fun addAll(newList: List<Album>){
        albums.clear()
        albums.addAll(newList)
        notifyDataSetChanged()
    }
}