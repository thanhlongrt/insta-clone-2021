package com.example.instagram.ui.notification

import android.app.NotificationManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.instagram.repository.NotificationRepository
import com.example.instagram.extensions.sendNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 5/7/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        serviceScope.launch(Dispatchers.IO) {
            notificationRepository.saveFcmTokenToLocal(token)
        }
        Log.e(TAG, "onNewToken: $token")

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        Log.e(TAG, "onMessageReceived: ${data["postId"]}")
        val notification = remoteMessage.notification


        remoteMessage.notification?.let {
            Log.e(TAG, "onMessageReceived: ${it.title} ${it.body}")
            sendNotification(it.body.toString(), data["senderAvatar"].toString())
        }

    }

    private fun sendNotification(body: String, largeIcon: String?) {
        val notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager

        notificationManager.sendNotification(body, applicationContext, largeIcon,0)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}