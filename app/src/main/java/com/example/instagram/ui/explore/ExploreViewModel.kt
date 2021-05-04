package com.example.instagram.ui.explore

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.DataState
import com.example.instagram.model.PostItem
import com.example.instagram.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 4/27/2021
 */

@ExperimentalCoroutinesApi
@HiltViewModel
class ExploreViewModel
@Inject
constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ExploreViewModel"
    }

    private val _feedPosts = MutableLiveData<DataState<List<PostItem>>>()
    val feedPosts: LiveData<DataState<List<PostItem>>> = _feedPosts

    fun getAllPosts() {
        _feedPosts.postValue(DataState.loading())
        viewModelScope.launch {
            postRepository.getFeedPostsWithLikes()
                .collect {
                    _feedPosts.value = it
                    Log.e(TAG, "getAllPosts: ${it.status}")
                }
        }
    }
}