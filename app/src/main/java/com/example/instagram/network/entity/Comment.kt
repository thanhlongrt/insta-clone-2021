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
    var like_count: Long = 0,
    val date_created: Long = 0
) {

    fun toMap(): HashMap<String, Any> {
        val data = HashMap<String, Any>()
        data["comment_id"] = comment_id
        data["uid"] = uid
        data["avatar"] = avatar
        data["username"] = username
        data["content"] = content
        data["post_id"] = post_id
        data["like_count"] = like_count
        data["date_created"] = date_created
        return data
    }
}