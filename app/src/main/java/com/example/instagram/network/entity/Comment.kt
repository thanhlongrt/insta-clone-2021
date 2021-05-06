package com.example.instagram.network.entity

/**
 * Created by Thanh Long Nguyen on 4/14/2021
 */
data class Comment(
    val comment_id: String = "",
    val uid: String = "",
    val avatar: String = "",
    val username: String = "",
    val content: String = "",
    val post_id: String = "",
    val date_created: Long = 0
) {
}