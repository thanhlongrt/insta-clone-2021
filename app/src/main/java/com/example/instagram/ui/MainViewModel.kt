package com.example.instagram.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.model.UserItem
import com.example.instagram.network.entity.Notification
import com.example.instagram.repository.NotificationRepository
import com.example.instagram.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 5/1/2021
 */
@ExperimentalCoroutinesApi
@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    application: Application
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val _userLivaData = MutableLiveData<UserItem>()
    val currentUser: LiveData<UserItem> = _userLivaData

    fun getCurrentUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getCurrentUserFromFirebase()
            userRepository.getUserFlow().collect { userItem ->
                userItem?.let {
                    _userLivaData.postValue(it)
                }
            }
        }
    }

    fun sendPushNotification(notification: Notification) {
        viewModelScope.launch(Dispatchers.IO) {
            notificationRepository.sendPushNotification(notification)
        }
    }

}