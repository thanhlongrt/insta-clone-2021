package com.example.instagram.ui.login

import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagram.DataState
import com.example.instagram.firebase_model.User
import com.example.instagram.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
@Inject
constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    companion object {
        const val TAG = "LoginViewModel"
    }

    var email = ObservableField("")
    var password = ObservableField("")
    var displayName = ObservableField("")

    val isLoggedIn: Boolean
        get() = userRepository.currentFirebaseUser != null

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
        Log.e(TAG, "login: Loading", )
        _loginResult.postValue(DataState.loading())
        userRepository.login(email.get()!!.trim(), password.get()!!.trim()).addOnCompleteListener {
            if (it.isSuccessful) {
                _loginResult.postValue(DataState.success(true))
                Log.e(TAG, "login: Success")
            } else {
                _loginResult.postValue(DataState.error(false, it.exception?.message.toString()))
                Log.e(TAG, "login: Failed: ${it.exception?.message.toString()}")
            }
        }
    }

    fun loginDataChanged() {
        if (!isEmailValid(email.get()!!)) {
            _loginForm.value = DataState.error(false, "Invalid email")
        } else if (!isPasswordValid(password.get()!!)) {
            _loginForm.value = DataState.error(false, "Password cannot be empty")
        } else {
            _loginForm.value = DataState.success(true)
        }
    }

    fun signUp() {
        _signUpResult.postValue(DataState.loading())
        Log.e(TAG, "signUp: Loading", )
        userRepository.createUser(email.get()!!.trim(), password.get()!!.trim()).addOnCompleteListener {
            if (it.isSuccessful) {
                val user = User(
                    uid = userRepository.currentFirebaseUser!!.uid,
                    email = email.get()!!,
                    username = email.get()!!.split("@")[0],
                    display_name = displayName.get()!!,
                    bio = "",
                    website = "",
                    profile_photo = "",
                    followers = 0,
                    following = 0,
                    posts = 0
                )

                saveUserData(user)
                _signUpResult.postValue(DataState.success(true))
                Log.e(TAG, "signUp: Success", )
            } else {
                _signUpResult.postValue(DataState.error(false, it.exception?.message.toString()))
                Log.e(TAG, "signUp: Failed: ${it.exception?.message.toString()}", )
            }
        }
    }

    private fun saveUserData(user: User) {
        val userData = HashMap<String, Any>()
        userData["uid"] = user.uid
        userData["email"] = user.email
        userData["username"] = user.username
        userData["display_name"] = user.display_name
        userData["bio"] = user.bio
        userData["website"] = user.website
        userData["profile_photo"] = user.profile_photo
        userData["followers"] = user.followers
        userData["following"] = user.following
        userData["posts"] = user.posts
        Log.e(TAG, "saveUserData: Loading", )
        userRepository.saveUserData(user.uid, userData).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.e(TAG, "saveUserData: Saved")
            } else {
                Log.e(TAG, "saveUserData: Failed: ${it.exception?.message.toString()}")
            }
        }

    }

    fun signUpDataChanged() {
        when {
            !isEmailValid(email.get()!!) -> {
                _signUpForm.value = DataState.error(false, "Invalid email")
            }
            !isPasswordValid(password.get()!!) -> {
                _signUpForm.value = DataState.error(false, "Password cannot be empty")
            }
            !isDisplayNameValid(displayName.get()!!) -> {
                _signUpForm.value = DataState.error(false, "Invalid display name")
            }
            else -> {
                _signUpForm.value = DataState.success(true)
            }
        }
    }

    // A placeholder username validation check
    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return !TextUtils.isEmpty(password)
    }

    private fun isDisplayNameValid(name: String): Boolean {
        return !TextUtils.isEmpty(name)
    }

    class LoginFormState(
        var isDataValid: Boolean = false,
        var emailError: String? = null,
        var passwordError: String? = null,
    ) {
        override fun toString(): String {
            return "($isDataValid, $emailError, $passwordError)"
        }
    }
}