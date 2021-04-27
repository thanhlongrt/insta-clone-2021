package com.example.instagram.ui.explore

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagram.DataState
import com.example.instagram.model.Post
import com.example.instagram.repository.PostRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _postsLiveData = MutableLiveData<DataState<List<Post>>>()
    val postsLiveData: LiveData<DataState<List<Post>>> = _postsLiveData

    fun getPosts() {
        _postsLiveData.postValue(DataState.loading())
        Log.e(TAG, "getPosts: Loading", )
        postRepository.postDataReference.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<Post>()
                for (item in snapshot.children) {
                    val post = item.getValue(Post::class.java)!!
                    posts.add(post)
                }
                _postsLiveData.postValue(DataState.success(posts))
                Log.e(TAG, "getPosts: Success: ${posts.size}", )
            }

            override fun onCancelled(error: DatabaseError) {
                _postsLiveData.postValue(DataState.error(null, error.message))
                Log.e(TAG, "getPosts: Error: ${error.message}", )
            }
        })
    }
}