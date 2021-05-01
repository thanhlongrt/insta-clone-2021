package com.example.instagram.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.DataState
import com.example.instagram.firebase_model.Story
import com.example.instagram.model.PostItem
import com.example.instagram.model.StoryItem
import com.example.instagram.repository.PostRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 4/28/2021
 */

@HiltViewModel
class HomeViewModel
@Inject
constructor(
    private val postRepository: PostRepository
) : ViewModel() {
    companion object{
        private const val TAG = "HomeViewModel"
    }

    private val _storiesLiveData = MutableLiveData<DataState<List<StoryItem>>>()
    val storiesLiveData: LiveData<DataState<List<StoryItem>>> = _storiesLiveData

    private val _userPosts = MutableLiveData<DataState<List<PostItem>>>()
    val userPosts: LiveData<DataState<List<PostItem>>>
        get() = _userPosts

    private val _likeId = MutableLiveData<DataState<String>>()
    val likeIdToDelete : LiveData<DataState<String>> = _likeId

    val currentUserUid = postRepository.currentUser!!.uid

    private var storyListener: ValueEventListener? = null
    fun getStoryData() {
        storyListener =
            postRepository.storyDataReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val firebaseStories = mutableListOf<Story>()
                    for (item in snapshot.children) {
                        val story = item.getValue(Story::class.java)!!
                        firebaseStories.add(story)
                    }

                    val userStories = mutableListOf<StoryItem>()
                    val storiesByUid = firebaseStories.groupBy { it.uid }
                    for (entry in storiesByUid){
                        val userStory = StoryItem(
                            uid = entry.key,
                            username = entry.value[0].username,
                            stories = entry.value
                        )
                        userStories.add(userStory)
                    }
                    _storiesLiveData.postValue(DataState.success(userStories))
                    Log.e(TAG, "getStoryData: Success ", )
                }

                override fun onCancelled(error: DatabaseError) {
                    _storiesLiveData.postValue(DataState.error(null, error.message))
                    Log.e(TAG, "getStoryData: Error: ${error.message} ", )
                }
            })
    }

    @ExperimentalCoroutinesApi
    fun getAllPosts() {
        _userPosts.postValue(DataState.loading())
        Log.e(TAG, "getPostById: Loading")
        viewModelScope.launch {
            postRepository.getAllPosts()
                .combine(postRepository.getAllLikes()) { postResult, likeResult ->
                    val likeByPost = likeResult.data!!.groupBy { it.post_id }
                    val posts = postResult.data!!
                    val postItems = posts.map { post ->
                        var isLiked = false
                        likeByPost[post.post_id]?.filter {
                            it.uid == currentUserUid && it.post_id == post.post_id
                        }?.size?.let {
                            isLiked = it > 0
                        }
                        PostItem(
                            postId = post.post_id,
                            uid = post.uid,
                            avatarUrl = post.avatar_url,
                            userName = post.user_name,
                            photoUrl = post.photo_url,
                            date = post.date_created,
                            caption = post.caption,
                            path = post.path,
                            likes = likeByPost[post.post_id] ?: mutableListOf(),
                            isLiked = isLiked
                        )
                    }
                    postItems
                }.catch { e ->
                    _userPosts.postValue(DataState.error(null, e.message!!))
                    Log.e(TAG, "getPostById: Error: ${e.message}")
                }
                .collect {
                    _userPosts.postValue(DataState.success(it))
                    Log.e(TAG, "getPostById: Success")
                }
        }
    }

    fun getLikeId(uid: String, postId: String){
        _likeId.postValue(DataState.loading())
        Log.e(TAG, "getLikeById: ", )
        viewModelScope.launch {
            postRepository.getLikeById(uid, postId)
                .catch { e ->
                    Log.e(TAG, "getLikeId: ${e.message}", )
                }
                .collect { result ->
                    if (result.data!!.isNotEmpty()){
                        _likeId.postValue(DataState.success(result.data[0].like_id))
                        Log.e(TAG, "getLikeId: Success", )
                    } else {
                        _likeId.postValue(DataState.error(null, "Not found"))
                    }
                }
        }
    }

    fun like(likeData: HashMap<String, Any>) {
        postRepository.like(likeData).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.e(TAG, "like: Success: ${likeData["like_id"]}")
            }
        }
    }

    fun unlike(likeId: String) {
        Log.e(TAG, "unlike: $likeId")
        postRepository.unlike(likeId)
    }

    override fun onCleared() {
        super.onCleared()
        postRepository.storyDataReference.removeEventListener(storyListener!!)
    }
}