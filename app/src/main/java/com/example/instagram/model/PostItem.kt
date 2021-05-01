package com.example.instagram.model

import com.example.instagram.firebase_model.Comment
import com.example.instagram.firebase_model.Like

/**
 * Created by Thanh Long Nguyen on 4/29/2021
 */
class PostItem(
    val postId: String = "",
    val uid: String = "",
    val avatarUrl: String = "",
    val userName: String = "",
    val photoUrl: String = "",
    val date: Long = 0,
    val caption: String = "",
    val likes: List<Like> = mutableListOf(),
    val comments: List<Comment> = mutableListOf(),
    val path: String,
    var isLiked: Boolean = false
) {
}