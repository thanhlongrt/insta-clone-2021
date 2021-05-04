package com.example.instagram.model

import com.example.instagram.network.entity.Story

/**
 * Created by Thanh Long Nguyen on 4/28/2021
 */
class UserStoryItem(
    val uid: String = "",
    val username: String = "",
    val avatarUrl: String = "",
    val stories: List<StoryItem> = mutableListOf(),
) {
}