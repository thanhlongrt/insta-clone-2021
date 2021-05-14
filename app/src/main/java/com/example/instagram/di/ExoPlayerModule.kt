package com.example.instagram.di

import android.content.Context
import com.example.instagram.Constants.MAX_CACHE_SIZE
import com.example.instagram.ui.profile.view_posts.CacheDataSourceFactory
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

/**
 * Created by Thanh Long Nguyen on 5/14/2021
 */
@Module
@InstallIn(SingletonComponent::class)
object ExoPlayerModule {

    @Singleton
    @Provides
    fun provideCacheEvictor(
    ): LeastRecentlyUsedCacheEvictor {
        return LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE)
    }

    @Singleton
    @Provides
    fun provideExoDatabaseProvider(
        @ApplicationContext context: Context
    ): ExoDatabaseProvider {
        return ExoDatabaseProvider(context)
    }

    @Singleton
    @Provides
    fun provideSimpleCache(
        evictor: LeastRecentlyUsedCacheEvictor,
        databaseProvider: ExoDatabaseProvider,
        @ApplicationContext context: Context
    ): SimpleCache {
        return SimpleCache(
            File(context.cacheDir, "media"),
            evictor,
            databaseProvider
        )
    }

    @Singleton
    @Provides
    fun provideDefaultDataSourceFactory(
        @ApplicationContext context: Context
    ): DefaultDataSourceFactory {
        val userAgent = Util.getUserAgent(context, context.packageName)
        return DefaultDataSourceFactory(
            context,
            DefaultBandwidthMeter.Builder(context).build(),
            DefaultHttpDataSource.Factory().setUserAgent(userAgent)
        )
    }

    @Singleton
    @Provides
    fun provideCacheDataSourceFactory(
        simpleCache: SimpleCache,
        defaultDataSourceFactory: DefaultDataSourceFactory
    ): CacheDataSourceFactory {
        return CacheDataSourceFactory(
            simpleCache,
            defaultDataSourceFactory
        )
    }
}