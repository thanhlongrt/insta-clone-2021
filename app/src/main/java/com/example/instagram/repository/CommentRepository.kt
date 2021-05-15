package com.example.instagram.repository

import com.example.instagram.DataState
import com.example.instagram.network.entity.Comment
import com.example.instagram.network.firebase.FirebaseService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 5/6/2021
 */
@ExperimentalCoroutinesApi
class CommentRepository
@Inject
constructor(
    private val firebaseService: FirebaseService
) {
    companion object {
        private const val TAG = "CommentRepository"
    }

    fun saveCommentData(comment: Comment) {
        firebaseService.saveCommentData(comment.toMap())
    }

    fun getCommentsByPost(postId: String) =
        firebaseService.getCommentsByPost(postId).map {
            DataState.success(it!!)
        }.catch { emit(DataState.error(null, it.message)) }
            .onStart { emit(DataState.loading()) }

}