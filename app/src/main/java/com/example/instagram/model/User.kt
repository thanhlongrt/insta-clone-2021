package com.example.instagram.model

import android.graphics.drawable.Drawable
import android.text.TextUtils
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by Thanh Long Nguyen on 4/12/2021
 */
data class User(
    var uid: String = "",
    var email: String = "",
    var username: String = "",
    var display_name: String = "",
    var bio: String = "",
    var website: String = "",
    var profile_photo: String = "",
    var followers: Long = 0,
    var following: Long = 0,
    var posts: Long = 0
) {

    companion object{
        @JvmStatic
        @BindingAdapter("imageUrl")
        fun loadImage(view: CircleImageView, url: String?) {
            if (!TextUtils.isEmpty(url)) {
                Glide.with(view.context)
                    .load(url)
                    .into(view)
            }

        }
    }

    override fun toString(): String {
        return username
    }
}