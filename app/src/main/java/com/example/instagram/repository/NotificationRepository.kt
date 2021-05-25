package com.example.instagram.repository

import android.util.Log
import com.example.instagram.DataState
import com.example.instagram.network.entity.*
import com.example.instagram.network.firebase.FirebaseService
import com.example.instagram.network.retrofit.FcmService
import com.example.instagram.room.dao.NotificationDao
import com.example.instagram.room.dao.StringKeyValueDao
import com.example.instagram.room.entity.StringKeyValuePair
import com.example.instagram.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 5/7/2021
 */

@ExperimentalCoroutinesApi
class NotificationRepository
@Inject
constructor(
    private val firebaseService: FirebaseService,
    private val stringKeyValueDao: StringKeyValueDao,
    private val fcmService: FcmService,
    private val notificationDao: NotificationDao
) {

    companion object {
        private const val TAG = "NotificationRepository"
    }

    suspend fun uploadFcmToken() {
        stringKeyValueDao.getTokenFlow(Constants.FCM_TOKEN).collect {
            it?.let {
                firebaseService.uploadFcmToken(it.value)
            }
        }
    }

    suspend fun saveFcmTokenToLocal(token: String) =
        stringKeyValueDao.insert(StringKeyValuePair(Constants.FCM_TOKEN, token))

    suspend fun sendPushNotification(notification: Notification) {
        firebaseService.getUserFcmToken(notification.uid).collect { token ->
            Log.e(TAG, "sendPushNotification: token: $token")
            token?.let {
                val fcmMessage = FcmMessage(
                    token = it,
                    notificationPayload = NotificationPayload(
                        title = notification.title,
                        body = notification.body
                    ),
                    dataPayload = DataPayload(
                        postId = notification.post_id,
                        senderAvatar = notification.sender_avatar
                    ),
                )
                fcmService.sendPushNotification(fcmMessage).run {
                    if (isSuccessful) {
                        Log.e(TAG, "sendPushNotification: ${body()?.success}")
                        firebaseService.saveNotification(notification)
                        notificationDao.insertNotification(notification)
                    }
                }
            }
        }
    }

    fun getNotification() = flow<DataState<List<Notification>>> {
        firebaseService.getNotifications().collect{
            emit(DataState.success(it!!))
        }
    }.onStart {
        getNotificationCache()?.let { emit(it) }
    }.catch {
        emit(DataState.error(null, it.message))
    }

    private suspend fun getNotificationCache() =
        currentUserUid?.let {
            notificationDao.getNotificationsByUser(it)?.let { notifications ->
                DataState.success(notifications)
            } ?: DataState.error(null, "getNotificationCache: Error")
        }

    private val currentUserUid get() = firebaseService.currentFirebaseUser?.uid

}