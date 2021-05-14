package com.example.instagram.repository

import android.util.Log
import com.example.instagram.DataState
import com.example.instagram.network.entity.Comment
import com.example.instagram.network.firebase.FirebaseService
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
    companion object{
        private const val TAG = "CommentRepository"
    }

    fun addComment(commentData: HashMap<String, Any>) {
        firebaseService.saveCommentData(commentData)
    }

    fun getCommentsFromFirebaseByPost(postId: String): Flow<DataState<List<Comment>>> =
        callbackFlow {
            val commentListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val comments = snapshot.children.mapNotNull { it.getValue(Comment::class.java) }

                    this@callbackFlow.sendBlocking(DataState.success(comments))
                }

                override fun onCancelled(error: DatabaseError) {
                    this@callbackFlow.sendBlocking(DataState.error(null, error.message))
                }
            }
            val commentRef = firebaseService.commentDataReference.child(postId)
            commentRef.addListenerForSingleValueEvent(commentListener)
            awaitClose { commentRef.removeEventListener(commentListener) }
        }

}