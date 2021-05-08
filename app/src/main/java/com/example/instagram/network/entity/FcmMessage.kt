package com.example.instagram.network.entity

import com.google.gson.annotations.SerializedName

/**
 * Created by Thanh Long Nguyen on 5/7/2021
 */
class FcmMessage(
    @SerializedName("to")
    val token: String = "",
    val collapseKey: String = "",
    val notificationPayload: NotificationPayload,
    val dataPayload: DataPayload
) {
}

class NotificationPayload(
    val title: String = "",
    val body: String = "",
) {
}

class DataPayload(
    val postId: String? = "",
    val senderAvatar: String? = "",
)