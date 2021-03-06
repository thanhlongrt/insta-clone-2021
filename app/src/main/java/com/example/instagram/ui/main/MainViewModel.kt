package com.example.instagram.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    fun getCurrentUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getCurrentUserFromFirebase()
        }
    }

}