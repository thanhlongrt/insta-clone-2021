package com.example.instagram.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagram.DataState
import com.example.instagram.firebase_model.Story
import com.example.instagram.model.UserStory
import com.example.instagram.repository.PostRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _storiesLiveData = MutableLiveData<DataState<List<UserStory>>>()
    val storiesLiveData: LiveData<DataState<List<UserStory>>> = _storiesLiveData

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

                    val userStories = mutableListOf<UserStory>()
                    val storiesByUid = firebaseStories.groupBy { it.uid }
                    for (entry in storiesByUid){
                        val userStory = UserStory(
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

    override fun onCleared() {
        super.onCleared()
        postRepository.storyDataReference.removeEventListener(storyListener!!)
    }
}