package com.example.instagram.ui.search

import androidx.appcompat.widget.SearchView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.*

/**
 * Created by Thanh Long Nguyen on 4/18/2021
 */

fun SearchView.getQueryTextChangeStateFlow(): StateFlow<String> {
    val query = MutableStateFlow("")

    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            query.value = newText!!
            return true
        }
    })
    return query
}

@ExperimentalCoroutinesApi
fun SearchView.getQueryTextChangeCallbackFlow() = callbackFlow<String>{
    val listener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            sendBlocking(newText!!)
            return true
        }
    }
    this@getQueryTextChangeCallbackFlow.setOnQueryTextListener(listener)
    awaitClose {  }
}.onStart { emit("") }

//fun SearchView.getQueryTextObservable(): Observable<String>{
//    val subject : BehaviorSubject<String> = BehaviorSubject.create()
//    subject.onNext("")
//
//    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//        override fun onQueryTextSubmit(query: String?): Boolean {
//            subject.onComplete()
//            return true
//        }
//
//        override fun onQueryTextChange(newText: String?): Boolean {
//            subject.onNext(newText)
//            return true
//        }
//    })
//    return subject
//}