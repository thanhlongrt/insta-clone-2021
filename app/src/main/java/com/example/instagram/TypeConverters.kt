package com.example.instagram

import com.example.instagram.network.entity.Post
import com.google.gson.Gson

/**
 * Created by Thanh Long Nguyen on 5/11/2021
 */
object TypeConverters {

    fun postToJson(post: Post): String {
        return Gson().toJson(post)
    }

    fun jsonToPost(json: String): Post {
        return Gson().fromJson(json, Post::class.java)
    }

}