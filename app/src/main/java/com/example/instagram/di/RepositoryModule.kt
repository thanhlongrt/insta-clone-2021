package com.example.instagram.di

import com.example.instagram.repository.FirebaseSource
import com.example.instagram.repository.PostRepository
import com.example.instagram.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by Thanh Long Nguyen on 4/13/2021
 */


@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideUserRepository(
        firebaseSource: FirebaseSource
    ): UserRepository {
        return UserRepository(firebaseSource)
    }

    @Singleton
    @Provides
    fun providePhotoRepository(
        firebaseSource: FirebaseSource
    ): PostRepository {
        return PostRepository(firebaseSource)
    }

}