package com.example.instagram.model

/**
 * Created by Thanh Long Nguyen on 4/14/2021
 */
class Comment(
    val uid: String = "",
    val comment: String = "",
    val photo_uid: String = "",
    val likes: Long,
    val date_created: String = ""
) {
}