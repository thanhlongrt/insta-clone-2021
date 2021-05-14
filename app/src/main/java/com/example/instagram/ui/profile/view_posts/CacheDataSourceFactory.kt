package com.example.instagram.ui.profile.view_posts

import com.example.instagram.Constants.MAX_FILE_SIZE
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 5/14/2021
 */
class CacheDataSourceFactory
@Inject
constructor(
    private val simpleCache: SimpleCache,
    private val defaultDataSourceFactory: DefaultDataSourceFactory
) : DataSource.Factory {

    override fun createDataSource(): DataSource {
        return CacheDataSource(
            simpleCache,
            defaultDataSourceFactory.createDataSource(),
            FileDataSource(),
            CacheDataSink(simpleCache, MAX_FILE_SIZE),
            CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
            null
        )
    }
}