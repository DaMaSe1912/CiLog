package com.damay.cilog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val favoritesRepository: FavoritesRepository
) {

    private val _manualUserUpdates = MutableSharedFlow<FirebaseUser?>(replay = 1)

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    val authState: Flow<FirebaseUser?> = merge(
        callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                trySend(firebaseAuth.currentUser)
            }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        },
        _manualUserUpdates
    )

    suspend fun reloadCurrentUser(): FirebaseUser? {
        val user = auth.currentUser ?: return null
        return try {
            user.reload().await()
            val freshUser = auth.currentUser
            _manualUserUpdates.emit(freshUser)
            freshUser
        } catch (e: Exception) {
            auth.currentUser
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw IllegalStateException("User is null")
            try {
                user.reload().await()
            } catch (e: Exception) {
                // Ignore reload failure if offline
            }
            val freshUser = auth.currentUser ?: user
            _manualUserUpdates.emit(freshUser)
            Result.success(freshUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(name: String, email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw IllegalStateException("User is null")

            if (name.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()
                try {
                    user.reload().await()
                } catch (e: Exception) {
                    // Ignore reload failure if offline
                }
            }

            // Sync user profile to Firestore
            try {
                val userData = hashMapOf(
                    "uid" to user.uid,
                    "displayName" to name,
                    "email" to email,
                    "createdAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(user.uid).set(userData).await()
            } catch (e: Exception) {
                // Ignore Firestore write failure so Auth account creation still succeeds
            }

            val freshUser = auth.currentUser ?: user
            _manualUserUpdates.emit(freshUser)
            Result.success(freshUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogleIdToken(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw IllegalStateException("User is null")
            try {
                user.reload().await()
            } catch (e: Exception) {
                // Ignore reload error
            }
            val freshUser = auth.currentUser ?: user
            _manualUserUpdates.emit(freshUser)
            Result.success(freshUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
        _manualUserUpdates.tryEmit(null)
    }

    suspend fun deleteAccount(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("No logged in user"))
        val uid = user.uid

        return try {
            // 1. Delete all Firestore favorites first (cascade delete)
            favoritesRepository.deleteAllUserData(uid)

            // 2. Delete Auth account
            user.delete().await()
            _manualUserUpdates.tryEmit(null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
