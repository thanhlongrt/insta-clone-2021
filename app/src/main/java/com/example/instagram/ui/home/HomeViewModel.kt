package com.example.instagram.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.DataState
import com.example.instagram.model.PostItem
import com.example.instagram.model.UserStoryItem
import com.example.instagram.network.entity.Notification
import com.example.instagram.repository.NotificationRepository
import com.example.instagram.repository.PostRepository
import com.example.instagram.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 4/28/2021
 */

@ExperimentalCoroutinesApi
@HiltViewModel
class HomeViewModel
@Inject
constructor(
    private val postRepository: PostRepository,
    private val storyRepository: StoryRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _stories = MutableLiveData<DataState<List<UserStoryItem>>>()
    val stories: LiveData<DataState<List<UserStoryItem>>>
        get() = _stories

    private val _feedPosts = MutableLiveData<DataState<List<PostItem>>>()
    val feedPosts: LiveData<DataState<List<PostItem>>>
        get() = _feedPosts

    val currentUserUid = postRepository.currentUser!!.uid

    fun getStoryData() {
        viewModelScope.launch(Dispatchers.IO) {
            storyRepository.getUserStories().collect {
                _stories.postValue(it)
                Log.e(TAG, "getStoryData: ${it.status}: ${it.message}")
            }
        }
    }

    fun getAllPosts() {
        viewModelScope.launch {
            postRepository.getFeedPosts()
                .collect {
                    _feedPosts.value = it
                    Log.e(TAG, "getAllPosts: ${it.status}")
                }
        }
    }

    fun clickLike(postId: String) {
        Log.e(TAG, "like: ")
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.onLikeClick(postId)
        }
    }

    fun sendPushNotification(notification: Notification) {
        viewModelScope.launch(Dispatchers.IO) {
            notificationRepository.sendPushNotification(notification)
        }
    }
}