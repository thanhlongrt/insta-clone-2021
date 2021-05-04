package com.example.instagram.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.DataState
import com.example.instagram.network.entity.User
import com.example.instagram.model.UserItem
import com.example.instagram.repository.PostRepository
import com.example.instagram.repository.UserRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
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
    application: Application
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val _userLivaData = MutableLiveData<DataState<UserItem>>()
    val userLiveData: LiveData<DataState<UserItem>> = _userLivaData

    fun getCurrentUser() {
        viewModelScope.launch {
            userRepository.getUser(userRepository.currentFirebaseUser!!.uid)
                .collect {
                    _userLivaData.value = it
                    Log.e(TAG, "getCurrentUser: ${it.status}", )
                }
        }
    }

}