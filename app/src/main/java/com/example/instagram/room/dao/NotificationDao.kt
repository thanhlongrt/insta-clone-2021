package com.example.instagram.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.network.entity.Notification

/**
 * Created by Thanh Long Nguyen on 5/8/2021
 */

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<Notification>)

    @Query("SELECT * FROM notification WHERE uid = :uid")
    suspend fun getNotificationsByUser(uid: String): List<Notification>?

}