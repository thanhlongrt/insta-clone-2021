package com.example.instagram.ui.login

import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.Constants
import com.example.instagram.DataState
import com.example.instagram.Status
import com.example.instagram.model.UserItem
import com.example.instagram.network.entity.User
import com.example.instagram.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class LoginViewModel
@Inject
constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    companion object {
        const val TAG = "LoginViewModel"
    }

    var email = MutableLiveData<String>("")
    var password = MutableLiveData<String>("")
    var displayName = MutableLiveData<String>("")

    val isLoggedIn: Boolean
        get() = userRepository.currentUser != null

    private val _loginForm = MutableLiveData<DataState<Boolean>>()
    val loginFormState: LiveData<DataState<Boolean>> = _loginForm

    private val _signUpForm = MutableLiveData<DataState<Boolean>>()
    val signUpFormState: LiveData<DataState<Boolean>> = _signUpForm

    private val _loginResult = MutableLiveData<DataState<Boolean>>()
    val loginResult: LiveData<DataState<Boolean>> = _loginResult

    private val _signUpResult = MutableLiveData<DataState<Boolean>>()
    val signUpResult: LiveData<DataState<Boolean>> = _signUpResult

    init {
        _loginForm.value = DataState.error(false, "")
    }


    fun login() {
        Log.e(TAG, "login: ...")
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.login(email.value!!.trim(), password.value!!.trim()).collect {
                _loginResult.postValue(it)
                Log.e(TAG, "login: ${it.status}", )
            }
        }
    }

    fun loginDataChanged() {
        if (!isEmailValid(email.value!!)) {
            _loginForm.value = DataState.error(false, "Invalid email")
        } else if (!isPasswordValid(password.value!!)) {
            _loginForm.value = DataState.error(false, "Password cannot be empty")
        } else {
            _loginForm.value = DataState.success(true)
        }
    }

    fun signUp() {
        _signUpResult.postValue(DataState.loading())
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.createUser(email.value!!.trim(), password.value!!.trim()).collect {
                Log.e(TAG, "signUp: ${it.status}", )
                _signUpResult.postValue(it)
                if (it.status == Status.SUCCESS) {
                    val user = UserItem(
                        uid = userRepository.currentUser!!.uid,
                        email = email.value!!,
                        username = email.value!!.split("@")[0],
                        displayName = displayName.value!!,
                        avatarUrl = Constants.DEFAULT_PROFILE_IMAGE_URL,
                    )
                    saveUserData(user)
                }
            }
        }
    }

    private suspend fun saveUserData(user: UserItem) {
        userRepository.saveUserData(user.uid, user)
    }

    fun signUpDataChanged() {
        when {
            !isEmailValid(email.value!!) -> {
                _signUpForm.value = DataState.error(false, "Invalid email")
            }
            !isPasswordValid(password.value!!) -> {
                _signUpForm.value = DataState.error(false, "Password cannot be empty")
            }
            !isDisplayNameValid(displayName.value!!) -> {
                _signUpForm.value = DataState.error(false, "Invalid display name")
            }
            else -> {
                _signUpForm.value = DataState.success(true)
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return !TextUtils.isEmpty(password)
    }

    private fun isDisplayNameValid(name: String): Boolean {
        return !TextUtils.isEmpty(name)
    }
}