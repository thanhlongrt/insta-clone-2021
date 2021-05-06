package com.example.instagram.ui.comment

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

/**
 * Created by Thanh Long Nguyen on 5/5/2021
 */
//@IgnoreExtraProperties
data class Test(
    var uid: String? = "",
    var author: String? = "",
    var title: String? = "",
    var body: String? = "",
    var starCount: Int = 0,
    var stars: MutableMap<String, Star> = HashMap()
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "author" to author,
            "title" to title,
            "body" to body,
            "starCount" to starCount,
            "stars" to stars
        )
    }
}