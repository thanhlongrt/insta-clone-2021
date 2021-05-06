package com.example.instagram.model

import com.example.instagram.network.entity.Comment

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
    var likeCount: Long = 0,
    val commentCount: Long = 0,
    var likes: MutableList<String> = mutableListOf(),
    val path: String,
    var isLiked: Boolean = false
) {
}