package com.example.instagram.room.dao

import androidx.annotation.NonNull
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.instagram.room.entity.StringKeyValuePair

/**
 * Created by Thanh Long Nguyen on 5/2/2021
 */
@Dao
interface StringKeyValueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keyValueData: StringKeyValuePair)

    @Query("SELECT * FROM value_pairs WHERE `key` = :key LIMIT 1")
    suspend fun get(@NonNull key: String): StringKeyValuePair?

}