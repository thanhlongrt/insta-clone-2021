package com.example.instagram.di

import com.example.instagram.network.firebase.FirebaseService
import com.example.instagram.network.entity.PostNetworkMapper
import com.example.instagram.network.entity.StoryNetworkMapper
import com.example.instagram.network.entity.UserNetworkMapper
import com.example.instagram.network.retrofit.FcmService
import com.example.instagram.repository.*
import com.example.instagram.room.dao.PostDao
import com.example.instagram.room.dao.StoryDao
import com.example.instagram.room.dao.StringKeyValueDao
import com.example.instagram.room.dao.UserDao
import com.example.instagram.room.entity.PostCacheMapper
import com.example.instagram.room.entity.UserCacheMapper
import com.example.instagram.room.entity.UserStoryCacheMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

/**
 * Created by Thanh Long Nguyen on 4/13/2021
 */


@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideUserRepository(
        firebaseService: FirebaseService,
        stringKeyValueDao: StringKeyValueDao,
        userDao: UserDao,
        userNetworkMapper: UserNetworkMapper,
        userCacheMapper: UserCacheMapper
    ): UserRepository {
        return UserRepository(
            firebaseService,
            stringKeyValueDao,
            userDao,
            userNetworkMapper,
            userCacheMapper
        )
    }

    @Singleton
    @Provides
    fun providePostRepository(
        firebaseService: FirebaseService,
        postDao: PostDao,
        postNetworkMapper: PostNetworkMapper,
        postCacheMapper: PostCacheMapper,
    ): PostRepository {
        return PostRepository(
            firebaseService,
            postDao,
            postNetworkMapper,
            postCacheMapper,
        )
    }

    @Singleton
    @Provides
    fun provideStoryRepository(
        firebaseService: FirebaseService,
        storyNetworkMapper: StoryNetworkMapper,
        storyDao: StoryDao,
        userStoryCacheMapper: UserStoryCacheMapper,
    ): StoryRepository {
        return StoryRepository(
            firebaseService,
            storyNetworkMapper,
            storyDao,
            userStoryCacheMapper
        )
    }

    @Singleton
    @Provides
    fun provideCommentRepository(
        firebaseService: FirebaseService
    ): CommentRepository {
        return CommentRepository(
            firebaseService
        )
    }

    @Singleton
    @Provides
    fun provideNotificationRepository(
        firebaseService: FirebaseService,
        stringKeyValueDao: StringKeyValueDao,
        fcmService: FcmService,
    ): NotificationRepository {
        return NotificationRepository(
            firebaseService,
            stringKeyValueDao,
            fcmService
        )
    }
}