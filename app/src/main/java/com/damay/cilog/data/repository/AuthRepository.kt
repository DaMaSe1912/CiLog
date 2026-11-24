package com.damay.cilog.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val favoritesRepository: FavoritesRepository
) {

    private val _manualUserUpdates = MutableSharedFlow<FirebaseUser?>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

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
            trySend(auth.currentUser)
            awaitClose { auth.removeAuthStateListener(listener) }
        },
        _manualUserUpdates
    )

    suspend fun reloadCurrentUser(): FirebaseUser? {
        val user = auth.currentUser ?: return null
        return try {
            withTimeoutOrNull(5_000) {
                user.reload().await()
            }
            val freshUser = auth.currentUser
            _manualUserUpdates.tryEmit(freshUser)
            freshUser
        } catch (e: Exception) {
            auth.currentUser
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = withTimeout(15_000) {
                auth.signInWithEmailAndPassword(email, password).await()
            }
            val user = result.user ?: throw IllegalStateException("User is null")
            val freshUser = auth.currentUser ?: user
            _manualUserUpdates.tryEmit(freshUser)
            Result.success(freshUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(name: String, email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = withTimeout(15_000) {
                auth.createUserWithEmailAndPassword(email, password).await()
            }
            val user = result.user ?: throw IllegalStateException("User is null")

            if (name.isNotBlank()) {
                try {
                    withTimeoutOrNull(5_000) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        user.updateProfile(profileUpdates).await()
                    }
                } catch (e: Exception) {
                    // Non-critical, ignore profile update failure
                }
            }

            // Sync user profile to Firestore in background without blocking Auth flow
            if (name.isNotBlank()) {
                try {
                    val userData = hashMapOf(
                        "uid" to user.uid,
                        "displayName" to name,
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )
                    // Fire-and-forget: do NOT await so account creation is never blocked
                    firestore.collection("users").document(user.uid).set(userData)
                } catch (e: Exception) {
                    // Non-critical, ignore Firestore write failure
                }
            }

            val freshUser = auth.currentUser ?: user
            _manualUserUpdates.tryEmit(freshUser)
            Result.success(freshUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogleIdToken(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = withTimeout(15_000) {
                auth.signInWithCredential(credential).await()
            }
            val user = result.user ?: throw IllegalStateException("User is null")
            val freshUser = auth.currentUser ?: user
            _manualUserUpdates.tryEmit(freshUser)
            Result.success(freshUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            withTimeout(15_000) {
                auth.sendPasswordResetEmail(email).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
        _manualUserUpdates.tryEmit(null)
    }

    suspend fun deleteAccount(password: String? = null): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Tidak ada akun yang sedang masuk"))
        val uid = user.uid
        val email = user.email

        return try {
            withTimeout(15_000) {
                // If password is provided, re-authenticate first to establish fresh credentials on Firebase Auth server
                if (!password.isNullOrBlank() && !email.isNullOrBlank()) {
                    val credential = EmailAuthProvider.getCredential(email, password)
                    user.reauthenticate(credential).await()
                }

                // Delete the user account from Firebase Auth
                user.delete().await()
            }

            // Successfully deleted on Firebase server!
            // 1. Immediately delete all local favorites and cached user data
            favoritesRepository.deleteAllUserData(uid)

            // 2. Sign out locally and notify all listeners
            auth.signOut()
            _manualUserUpdates.tryEmit(null)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
