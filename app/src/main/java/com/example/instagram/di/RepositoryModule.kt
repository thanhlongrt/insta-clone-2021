package com.example.instagram.di

import com.example.instagram.network.FirebaseSource
import com.example.instagram.network.entity.PostNetworkMapper
import com.example.instagram.network.entity.StoryNetworkMapper
import com.example.instagram.network.entity.UserNetworkMapper
import com.example.instagram.repository.CommentRepository
import com.example.instagram.repository.PostRepository
import com.example.instagram.repository.StoryRepository
import com.example.instagram.repository.UserRepository
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
        firebaseSource: FirebaseSource,
        stringKeyValueDao: StringKeyValueDao,
        userDao: UserDao,
        userNetworkMapper: UserNetworkMapper,
        userCacheMapper: UserCacheMapper
    ): UserRepository {
        return UserRepository(
            firebaseSource,
            stringKeyValueDao,
            userDao,
            userNetworkMapper,
            userCacheMapper
        )
    }

    @Singleton
    @Provides
    fun providePostRepository(
        firebaseSource: FirebaseSource,
        postDao: PostDao,
        postNetworkMapper: PostNetworkMapper,
        postCacheMapper: PostCacheMapper,
    ): PostRepository {
        return PostRepository(
            firebaseSource,
            postDao,
            postNetworkMapper,
            postCacheMapper,
        )
    }

    @Singleton
    @Provides
    fun provideStoryRepository(
        firebaseSource: FirebaseSource,
        storyNetworkMapper: StoryNetworkMapper,
        storyDao: StoryDao,
        userStoryCacheMapper: UserStoryCacheMapper,
    ): StoryRepository {
        return StoryRepository(
            firebaseSource,
            storyNetworkMapper,
            storyDao,
            userStoryCacheMapper
        )
    }

    @Singleton
    @Provides
    fun provideCommentRepository(
        firebaseSource: FirebaseSource
    ): CommentRepository {
        return CommentRepository(
            firebaseSource
        )
    }
}