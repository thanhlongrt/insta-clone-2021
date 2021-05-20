package com.example.instagram.network.entity

import com.example.instagram.EntityMapper
import com.example.instagram.model.UserItem
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 4/12/2021
 */
data class User(
    var uid: String = "",
    var email: String = "",
    var username: String = "",
    var display_name: String = "",
    var bio: String = "",
    var website: String = "",
    var profile_photo: String = "",
    var follower_count: Long = 0,
    var followers: MutableMap<String, Boolean> = HashMap(),
    var following_count: Long = 0,
    var following: MutableMap<String, Boolean> = HashMap(),
    var post_count: Long = 0
) {
    fun toMap(): HashMap<String, Any> {
        val data = HashMap<String, Any>()
        data["uid"] = uid
        data["email"] = email
        data["username"] = username
        data["display_name"] = display_name
        data["bio"] = bio
        data["website"] = website
        data["profile_photo"] = profile_photo
        data["follower_count"] = follower_count
        data["followers"] = followers
        data["following_count"] = following_count
        data["following"] = following
        data["post_count"] = post_count
        return data
    }

    override fun toString(): String {
        return username
    }
}

class UserNetworkMapper @Inject constructor() : EntityMapper<User, UserItem> {
    override fun fromEntity(entity: User): UserItem {
        return UserItem(
            uid = entity.uid,
            email = entity.email,
            username = entity.username,
            displayName = entity.display_name,
            bio = entity.bio,
            website = entity.website,
            avatarUrl = entity.profile_photo,
            postCount = entity.post_count,
            followerCount = entity.follower_count,
            followers = entity.followers.keys.toMutableList(),
            followingCount = entity.following_count,
            following = entity.following.keys.toMutableList()
        )
    }

    override fun fromModel(model: UserItem): User {
        return User(
            uid = model.uid,
            email = model.email,
            username = model.username,
            display_name = model.displayName,
            bio = model.bio,
            website = model.website,
            profile_photo = model.avatarUrl,
            post_count = model.postCount,
            follower_count = model.followerCount,
            following_count = model.followingCount
        )
    }

}