package com.example.instagram.extensions

import androidx.lifecycle.MutableLiveData

/**
 * Created by Thanh Long Nguyen on 5/20/2021
 */

fun <T> MutableLiveData<T>.notifyObserver(){
    this.postValue(this.value)
}