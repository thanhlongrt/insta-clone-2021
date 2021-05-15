package com.example.instagram.room.entity

import androidx.room.Entity
import com.example.instagram.EntityMapper
import com.example.instagram.model.UserItem
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 5/3/2021
 */
@Entity(tableName = "users", primaryKeys = ["uid"])
data class UserCache(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val displayName: String = "",
    var postCount: Long = 0,
    var followerCount: Long = 0,
    var followingCount: Long = 0,
    val bio: String = "",
    val website: String = "",
    val avatarUrl: String = "",
) {
}

class UserCacheMapper @Inject constructor() : EntityMapper<UserCache, UserItem> {
    override fun fromEntity(entity: UserCache): UserItem {
        return UserItem(
            uid = entity.uid,
            email = entity.email,
            username = entity.username,
            displayName = entity.displayName,
            postCount = entity.postCount,
            followerCount = entity.followerCount,
            followingCount = entity.followingCount,
            bio = entity.bio,
            website = entity.website,
            avatarUrl = entity.avatarUrl,
        )
    }

    override fun fromModel(model: UserItem): UserCache {
        return UserCache(
            uid = model.uid,
            email = model.email,
            username = model.username,
            displayName = model.displayName,
            postCount = model.postCount,
            followerCount = model.followerCount,
            followingCount = model.followingCount,
            bio = model.bio,
            website = model.website,
            avatarUrl = model.avatarUrl,
        )
    }

}