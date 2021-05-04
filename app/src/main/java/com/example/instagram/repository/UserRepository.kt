package com.example.instagram.repository

import com.example.instagram.DataState
import com.example.instagram.model.UserItem
import com.example.instagram.network.FirebaseSource
import com.example.instagram.network.entity.User
import com.example.instagram.network.entity.UserNetworkMapper
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
    private val firebaseSource: FirebaseSource,
    private val stringKeyValueDao: StringKeyValueDao,
    private val userDao: UserDao,
    private val userNetworkMapper: UserNetworkMapper,
    private val userCacheMapper: UserCacheMapper
) {
    private val cacheThresholdInMillis = 5 * 60 * 1000L

    fun logout() = firebaseSource.logout()

    val currentFirebaseUser
        get() = firebaseSource.currentFirebaseUser


    fun createUser(email: String, password: String) =
        firebaseSource.createUser(email, password)

    fun login(email: String, password: String) =
        firebaseSource.login(email, password)

    fun saveUserData(uid: String, userData: HashMap<String, Any>) =
        firebaseSource.saveUserData(uid, userData)

    fun allUserDataReference() =
        firebaseSource.allUsersDataReference()

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

        firebaseSource.userDataReference(uid).addValueEventListener(userListener)

        awaitClose {
            firebaseSource.userDataReference(uid).removeEventListener(userListener)
        }
    }


    fun updateUserData(userData: HashMap<String, Any>): Flow<DataState<Boolean>> = channelFlow {
        firebaseSource.saveUserData(currentFirebaseUser!!.uid, userData)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    this.sendBlocking(DataState.success(true))
                } else {
                    this.sendBlocking(DataState.error(null, it.exception?.message!!))
                }
            }
        awaitClose {

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