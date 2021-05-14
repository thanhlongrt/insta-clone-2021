package com.example.instagram.network.entity

import androidx.room.Entity

/**
 * Created by Thanh Long Nguyen on 5/8/2021
 */

@Entity(tableName = "notifications", primaryKeys = ["notification_id"])
class Notification(
    val uid: String = "",
    val notification_id: String = "",
    val post_id: String = "",
    val title: String = "",
    val body: String = "",
    val date: Long = 0,
    val sender_avatar: String = "",
    var seen: Boolean = false
) {

    fun toMap(): HashMap<String, Any> {
        val data = HashMap<String, Any>()
        data["uid"] = uid
        data["notification_id"] = notification_id
        data["post_id"] = post_id
        data["title"] = title
        data["body"] = body
        data["date"] = date
        data["sender_avatar"] = sender_avatar
        data["seen"] = seen
        return data
    }
}