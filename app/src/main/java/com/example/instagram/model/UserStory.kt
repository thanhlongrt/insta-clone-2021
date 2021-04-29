package com.example.instagram.model

import com.example.instagram.firebase_model.Story

/**
 * Created by Thanh Long Nguyen on 4/28/2021
 */
class UserStory(
    val uid: String = "",
    val username: String = "",
    val stories: List<Story> = mutableListOf(),
) {
}