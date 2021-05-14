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
    var followers: Long = 0,
    var following: Long = 0,
    var posts: Long = 0
) {

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
        )
    }

}