package com.example.instagram.ui.notification

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.DataState
import com.example.instagram.network.entity.Notification
import com.example.instagram.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 5/7/2021
 */

@ExperimentalCoroutinesApi
@HiltViewModel
class NotificationViewModel
@Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    companion object {
        private const val TAG = "NotificationViewModel"
    }

    private val _notification = MutableLiveData<DataState<List<Notification>>>()
    val notification: LiveData<DataState<List<Notification>>>
        get() = _notification

    fun getNotification() {
        viewModelScope.launch(Dispatchers.IO) {
            notificationRepository.getNotification().collect {
                _notification.postValue(it)
            }
        }
    }

    fun uploadFcmToken() {
        viewModelScope.launch(Dispatchers.IO) {
            notificationRepository.uploadFcmToken()
        }
    }

    fun sendPushNotification(
        receiverUid: String,
        notificationTitle: String,
        notificationBody: String,
        postId: String?,
        avatar: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.e(TAG, "sendPushNotification: ")
            notificationRepository.sendPushNotification(
                receiverUid,
                notificationTitle,
                notificationBody,
                postId,
                avatar
            )
        }
    }

}