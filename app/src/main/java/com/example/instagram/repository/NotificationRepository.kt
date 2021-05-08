package com.example.instagram.repository

import android.util.Log
import com.example.instagram.Constants
import com.example.instagram.DataState
import com.example.instagram.network.entity.*
import com.example.instagram.network.firebase.FirebaseService
import com.example.instagram.network.retrofit.FcmService
import com.example.instagram.room.dao.NotificationDao
import com.example.instagram.room.dao.StringKeyValueDao
import com.example.instagram.room.entity.StringKeyValuePair
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
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
        getUserFcmToken(notification.uid).collect { token ->
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
                        saveNotification(notification)
                        notificationDao.insertNotification(notification)
                    }
                }
            }
        }
    }

    private fun saveNotification(
        notification: Notification
    ) {
        val notificationData = HashMap<String, Any>()
        firebaseService.saveNotification(notification)
    }

    private fun getUserFcmToken(uid: String) = callbackFlow<String?> {
        val tokenRef = firebaseService.userFcmTokenReference(uid)
        val tokenListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val token = snapshot.getValue(FcmToken::class.java)!!
                this@callbackFlow.sendBlocking(token.token)
            }

            override fun onCancelled(error: DatabaseError) {
                this@callbackFlow.sendBlocking(null)
            }
        }

        tokenRef.addListenerForSingleValueEvent(tokenListener)
        awaitClose {
        }
    }

    fun getNotification() = callbackFlow<DataState<List<Notification>>> {
        currentUserUid?.let { uid ->
            val notificationRef = firebaseService.notificationReference.child(uid)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notifications =
                        snapshot.children.mapNotNull { it.getValue(Notification::class.java) }
                    this@callbackFlow.sendBlocking(DataState.success(notifications))
                }

                override fun onCancelled(error: DatabaseError) {
                    this@callbackFlow.sendBlocking(DataState.error(null, error.message))
                }

            }
            notificationRef.addValueEventListener(listener)
            awaitClose { notificationRef.removeEventListener(listener) }
        }
    }.onStart {
        getNotificationCache()?.let { emit(it) }
    }

    private suspend fun getNotificationCache() =
        currentUserUid?.let {
            notificationDao.getNotificationsByUser(it)?.let { notifications ->
                DataState.success(notifications)
            } ?: DataState.error(null, "getNotificationCache: Error")
        }

    private val currentUserUid = firebaseService.currentFirebaseUser?.uid

}