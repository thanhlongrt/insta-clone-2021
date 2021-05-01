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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 4/27/2021
 */

@HiltViewModel
class ExploreViewModel
@Inject
constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    companion object{
        private const val TAG = "ExploreViewModel"
    }

    private val _postsLiveData = MutableLiveData<DataState<List<PostItem>>>()
    val postsLiveData: LiveData<DataState<List<PostItem>>> = _postsLiveData

    @ExperimentalCoroutinesApi
    fun getAllPosts() {
        _postsLiveData.postValue(DataState.loading())
        Log.e(TAG, "getAllPosts: Loading", )
        viewModelScope.launch {
            postRepository.getAllPosts()
                .combine(postRepository.getAllLikes()) { postResult, likeResult ->

                    val likeByPost = likeResult.data!!.groupBy { it.post_id }
                    val posts = postResult.data!!
                    val postItems = posts.map { post ->
                        PostItem(
                            postId = post.post_id,
                            uid = post.uid,
                            avatarUrl = post.avatar_url,
                            photoUrl = post.photo_url,
                            date = post.date_created,
                            caption = post.caption,
                            path = post.path,
                            likes = likeByPost[post.post_id] ?: mutableListOf()
                        )
                    }
                    postItems
                }.catch { e ->
                    _postsLiveData.postValue(DataState.error(null, e.message!!))
                    Log.e(TAG, "getAllPosts: Error: ${e.message}")
                }
                .collect {
                    _postsLiveData.postValue(DataState.success(it))
                    Log.e(TAG, "getAllPosts: Success: ${it.size}", )
                }
        }
    }
    @ExperimentalCoroutinesApi
    fun getAllPostsWithoutLike() {
        _postsLiveData.postValue(DataState.loading())
        Log.e(TAG, "getAllPosts: Loading", )
        viewModelScope.launch {
            postRepository.getAllPosts()
                .catch { e ->
                    _postsLiveData.postValue(DataState.error(null, e.message!!))
                    Log.e(TAG, "getAllPostsWithoutLike: Error: ${e.message}")
                }
                .collect { postResult ->
                    val posts = postResult.data!!
                    val postItems = posts.map { post ->
                        PostItem(
                            postId = post.post_id,
                            uid = post.uid,
                            avatarUrl = post.avatar_url,
                            photoUrl = post.photo_url,
                            date = post.date_created,
                            caption = post.caption,
                            path = post.path,
                            likes = mutableListOf()
                        )
                    }
                    _postsLiveData.postValue(DataState.success(postItems))
                    Log.e(TAG, "getAllPostsWithoutLike: Success: ${postItems.size}", )
                }
        }
    }

}