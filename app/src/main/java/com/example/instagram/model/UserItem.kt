package com.example.instagram.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Thanh Long Nguyen on 5/1/2021
 */

class UserItem(
    var uid: String = "",
    var email: String = "",
    var username: String = "",
    var displayName: String = "",
    var bio: String = "",
    var website: String = "",
    var avatarUrl: String = "",
) {
}