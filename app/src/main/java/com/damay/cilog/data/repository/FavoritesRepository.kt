package com.damay.cilog.data.repository

import com.damay.cilog.data.model.FavoriteMovie
import com.damay.cilog.data.model.Movie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private val currentUid: String?
        get() = auth.currentUser?.uid

    fun getFavorites(): Flow<List<FavoriteMovie>> {
        val uid = currentUid ?: return flowOf(emptyList())

        return callbackFlow {
            val listener = firestore.collection("users")
                .document(uid)
                .collection("favorites")
                .orderBy("addedAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val favorites = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(FavoriteMovie::class.java)
                    } ?: emptyList()
                    trySend(favorites)
                }

            awaitClose { listener.remove() }
        }
    }

    fun isFavorite(movieId: Int): Flow<Boolean> {
        val uid = currentUid ?: return flowOf(false)

        return callbackFlow {
            val listener = firestore.collection("users")
                .document(uid)
                .collection("favorites")
                .document(movieId.toString())
                .addSnapshotListener { snapshot, _ ->
                    trySend(snapshot?.exists() == true)
                }

            awaitClose { listener.remove() }
        }
    }

    suspend fun addFavorite(movie: Movie): Result<Unit> {
        val uid = currentUid ?: return Result.failure(IllegalStateException("User not logged in"))

        return try {
            val fav = FavoriteMovie(
                movieId = movie.id,
                title = movie.title,
                posterPath = movie.posterUrl,
                releaseDate = movie.releaseDate,
                voteAverage = movie.voteAverage,
                addedAt = System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(uid)
                .collection("favorites")
                .document(movie.id.toString())
                .set(fav)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFavorite(movieId: Int): Result<Unit> {
        val uid = currentUid ?: return Result.failure(IllegalStateException("User not logged in"))

        return try {
            firestore.collection("users")
                .document(uid)
                .collection("favorites")
                .document(movieId.toString())
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAllUserData(uid: String): Result<Unit> {
        return try {
            // Delete all favorite documents in batch to avoid orphan docs
            val snapshot = firestore.collection("users")
                .document(uid)
                .collection("favorites")
                .get()
                .await()

            val batch = firestore.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.delete(firestore.collection("users").document(uid))
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
