package com.example.instagram.model

/**
 * Created by Thanh Long Nguyen on 5/1/2021
 */

class UserItem(
    var uid: String = "",
    var email: String = "",
    var username: String = "",
    var displayName: String = "",
    var postCount: Long = 0,
    var followerCount: Long = 0,
    var followers: MutableList<String> = mutableListOf(),
    var followingCount: Long = 0,
    var following: MutableList<String> = mutableListOf(),
    var bio: String = "",
    var website: String = "",
    var avatarUrl: String = "",
    var isFollowed: Boolean = false,
) {
}