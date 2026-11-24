package com.damay.cilog.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.damay.cilog.data.model.FavoriteMovie
import com.damay.cilog.data.model.Movie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.favoritesDataStore: DataStore<Preferences> by preferencesDataStore(name = "cilog_favorites")

@Singleton
class FavoritesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val authStateFlow: Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser?.uid)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    private fun getUidKey(uid: String?): String = uid ?: "guest"

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFavorites(): Flow<List<FavoriteMovie>> = authStateFlow.flatMapLatest { uid ->
        val key = stringPreferencesKey("fav_${getUidKey(uid)}")
        context.favoritesDataStore.data.map { preferences ->
            val raw = preferences[key]
            if (raw.isNullOrBlank()) {
                emptyList()
            } else {
                try {
                    json.decodeFromString<List<FavoriteMovie>>(raw)
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun isFavorite(movieId: Int): Flow<Boolean> = getFavorites().map { list ->
        list.any { it.movieId == movieId }
    }

    suspend fun addFavorite(movie: Movie): Result<Unit> {
        return try {
            val uid = getUidKey(auth.currentUser?.uid)
            val fav = FavoriteMovie(
                movieId = movie.id,
                title = movie.title,
                posterPath = movie.posterUrl,
                releaseDate = movie.releaseDate,
                voteAverage = movie.voteAverage,
                addedAt = System.currentTimeMillis()
            )

            val key = stringPreferencesKey("fav_$uid")
            context.favoritesDataStore.edit { preferences ->
                val currentList = try {
                    preferences[key]?.let { json.decodeFromString<List<FavoriteMovie>>(it) } ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
                val updated = listOf(fav) + currentList.filterNot { it.movieId == movie.id }
                preferences[key] = json.encodeToString(updated)
            }

            // Sync to Firestore in background without blocking UI
            val userUid = auth.currentUser?.uid
            if (userUid != null) {
                try {
                    firestore.collection("users")
                        .document(userUid)
                        .collection("favorites")
                        .document(movie.id.toString())
                        .set(fav)
                } catch (e: Exception) {
                    // Ignore cloud failure
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFavorite(movieId: Int): Result<Unit> {
        return try {
            val uid = getUidKey(auth.currentUser?.uid)
            val key = stringPreferencesKey("fav_$uid")
            context.favoritesDataStore.edit { preferences ->
                val currentList = try {
                    preferences[key]?.let { json.decodeFromString<List<FavoriteMovie>>(it) } ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
                val updated = currentList.filterNot { it.movieId == movieId }
                preferences[key] = json.encodeToString(updated)
            }

            // Sync removal to Firestore in background
            val userUid = auth.currentUser?.uid
            if (userUid != null) {
                try {
                    firestore.collection("users")
                        .document(userUid)
                        .collection("favorites")
                        .document(movieId.toString())
                        .delete()
                } catch (e: Exception) {
                    // Ignore cloud failure
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAllUserData(uid: String): Result<Unit> {
        return try {
            val key = stringPreferencesKey("fav_$uid")
            context.favoritesDataStore.edit { preferences ->
                preferences.remove(key)
            }

            val guestKey = stringPreferencesKey("fav_guest")
            context.favoritesDataStore.edit { preferences ->
                preferences.remove(guestKey)
            }

            try {
                firestore.collection("users").document(uid).delete()
            } catch (e: Exception) {
                // Ignore cloud cleanup error
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
