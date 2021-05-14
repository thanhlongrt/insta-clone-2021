package com.example.instagram

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 4/12/2021
 */

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(hiltWorkerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }

//    override fun onCreate() {
//        super.onCreate()
//        AppInitializer.getInstance(this)
//            .initializeComponent(CustomWorkManagerInitializer::class.java)
//    }
}