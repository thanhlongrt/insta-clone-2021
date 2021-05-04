package com.example.instagram.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import com.example.instagram.model.UserItem
import com.example.instagram.room.entity.UserCache

/**
 * Created by Thanh Long Nguyen on 5/1/2021
 */
@Dao
interface UserDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertUser(user: UserCache)

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUser(): UserCache?

    @Query("DELETE FROM users")
    suspend fun deleteAll()

    @Transaction
    suspend fun deleteAllAndInsert(user: UserCache) {
        deleteAll()
        insertUser(user)
    }

}