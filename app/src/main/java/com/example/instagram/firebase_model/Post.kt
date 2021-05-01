package com.example.instagram.firebase_model

/**
 * Created by Thanh Long Nguyen on 4/14/2021
 */
class Post(
    val uid: String = "",
    val post_id: String = "",
    val avatar_url: String = "",
    val user_name: String = "",
    val photo_url: String = "",
    val date_created: Long = 0,
    val caption: String = "",
    val likes: Long = 0,
    val comments: Long = 0,
    val path: String = ""
){
}