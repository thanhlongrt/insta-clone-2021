package com.example.instagram.ui.notification

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

    private val _notifications = MutableLiveData<DataState<List<Notification>>>()
    val notifications: LiveData<DataState<List<Notification>>>
        get() = _notifications

    fun getNotification() {
        viewModelScope.launch(Dispatchers.IO) {
            notificationRepository.getNotification().collect {
                _notifications.postValue(it)
            }
        }
    }

    fun uploadFcmToken() {
        viewModelScope.launch(Dispatchers.IO) {
            notificationRepository.uploadFcmToken()
        }
    }

}