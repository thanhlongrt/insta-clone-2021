package com.example.instagram.repository

import android.util.Log
import com.example.instagram.DataState
import com.example.instagram.model.UserItem
import com.example.instagram.network.entity.User
import com.example.instagram.network.entity.UserNetworkMapper
import com.example.instagram.network.firebase.FirebaseService
import com.example.instagram.room.dao.StringKeyValueDao
import com.example.instagram.room.dao.UserDao
import com.example.instagram.room.entity.UserCacheMapper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.*
import javax.inject.Inject


@ExperimentalCoroutinesApi
class UserRepository
@Inject
constructor(
    private val firebaseService: FirebaseService,
    private val stringKeyValueDao: StringKeyValueDao,
    private val userDao: UserDao,
    private val userNetworkMapper: UserNetworkMapper,
    private val userCacheMapper: UserCacheMapper
) {

    companion object{
        private const val TAG = "UserRepository"
    }
    private val cacheThresholdInMillis = 5 * 60 * 1000L

    fun logout() = firebaseService.logout()

    val currentFirebaseUser
        get() = firebaseService.currentFirebaseUser


    fun createUser(email: String, password: String) =
        firebaseService.createUser(email, password)

    fun login(email: String, password: String) =
        firebaseService.login(email, password)

    fun saveUserData(uid: String, userData: HashMap<String, Any>) =
        firebaseService.saveUserData(uid, userData)

    fun allUserDataReference() =
        firebaseService.allUsersDataReference()

    fun getUser(uid: String): Flow<DataState<UserItem>> = flow {
        emit(getUserFromCache())
        getUserFromFirebase(uid)
            .collect {
                emit(DataState.success(it))
                if (it.uid == currentFirebaseUser!!.uid) {
                    userDao.deleteAllAndInsert(userCacheMapper.fromModel(it))
                }
            }
    }

    private suspend fun getUserFromCache() =
        userDao.getUser()?.let { user ->
            DataState.success(userCacheMapper.fromEntity(user))
        } ?: DataState.error(null, "getUserCache: Error")

    private fun getUserFromFirebase(uid: String) = callbackFlow<UserItem> {
        val userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)!!
                val userItem = userNetworkMapper.fromEntity(user)
                this@callbackFlow.sendBlocking(userItem)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        }

        firebaseService.userDataReference(uid).addValueEventListener(userListener)

        awaitClose {
            firebaseService.userDataReference(uid).removeEventListener(userListener)
        }
    }


    fun updateUserData(userData: HashMap<String, Any>): Flow<DataState<Boolean>> = callbackFlow {
        firebaseService.saveUserData(currentFirebaseUser!!.uid, userData)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    this.sendBlocking(DataState.success(true))
                } else {
                    this.sendBlocking(DataState.error(null, it.exception?.message!!))
                }
            }
        awaitClose {
            Log.e(TAG, "updateUserData: await close", )
        }
    }.onStart {
        emit(DataState.loading())
    }

    fun shouldFetchFirebaseData(
        lastFetchTimeMillis: String,
        cacheThresholdInMillis: Long = 300000L // default value is 5 minutes
    ): Boolean {
        return (System.currentTimeMillis() - lastFetchTimeMillis.toLong()) >= cacheThresholdInMillis
    }

}