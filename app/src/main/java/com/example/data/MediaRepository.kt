package com.example.data

import android.content.Context
import com.example.R
import com.example.model.MediaItem
import com.example.model.Playlist
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class MediaRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    // Observe all media items for user in realtime
    fun observeMediaItems(userId: String): Flow<List<MediaItem>> {
        return db.collection("users")
            .document(userId)
            .collection("media_items")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(MediaItem::class.java)
            }
    }

    // Observe all playlists for user in realtime
    fun observePlaylists(userId: String): Flow<List<Playlist>> {
        return db.collection("users")
            .document(userId)
            .collection("playlists")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Playlist::class.java)
            }
    }

    // Add new media item
    suspend fun addMediaItem(userId: String, item: MediaItem): Result<String> {
        val path = "users/$userId/media_items"
        return try {
            val collectionRef = db.collection("users").document(userId).collection("media_items")
            val docRef = if (item.id.isNotBlank() && item.id.matches(Regex("^[a-zA-Z0-9_\\-]+$"))) {
                collectionRef.document(item.id)
            } else {
                collectionRef.document()
            }

            val payload = mutableMapOf<String, Any>(
                "userId" to userId,
                "title" to item.title.trim(),
                "type" to item.type,
                "isFavorite" to item.isFavorite,
                "tags" to item.tags,
                "playlistIds" to item.playlistIds,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            item.url?.takeIf { it.isNotBlank() }?.let { payload["url"] = it.trim() }
            item.content?.takeIf { it.isNotBlank() }?.let { payload["content"] = it.trim() }
            item.artistOrAuthor?.takeIf { it.isNotBlank() }?.let { payload["artistOrAuthor"] = it.trim() }
            item.category?.takeIf { it.isNotBlank() }?.let { payload["category"] = it.trim() }
            item.thumbnailUrl?.takeIf { it.isNotBlank() }?.let { payload["thumbnailUrl"] = it.trim() }

            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // Update existing media item
    suspend fun updateMediaItem(userId: String, item: MediaItem): Result<Unit> {
        val path = "users/$userId/media_items/${item.id}"
        return try {
            require(item.id.isNotBlank()) { "Media item id must not be blank for update" }
            val docRef = db.collection("users").document(userId).collection("media_items").document(item.id)
            val payload = mutableMapOf<String, Any>(
                "title" to item.title.trim(),
                "type" to item.type,
                "isFavorite" to item.isFavorite,
                "tags" to item.tags,
                "playlistIds" to item.playlistIds,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            item.url?.takeIf { it.isNotBlank() }?.let { payload["url"] = it.trim() }
            item.content?.takeIf { it.isNotBlank() }?.let { payload["content"] = it.trim() }
            item.artistOrAuthor?.takeIf { it.isNotBlank() }?.let { payload["artistOrAuthor"] = it.trim() }
            item.category?.takeIf { it.isNotBlank() }?.let { payload["category"] = it.trim() }
            item.thumbnailUrl?.takeIf { it.isNotBlank() }?.let { payload["thumbnailUrl"] = it.trim() }

            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // Delete media item
    suspend fun deleteMediaItem(userId: String, itemId: String): Result<Unit> {
        val path = "users/$userId/media_items/$itemId"
        return try {
            db.collection("users").document(userId).collection("media_items").document(itemId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    // Toggle favorite state
    suspend fun toggleFavorite(userId: String, itemId: String, isFavorite: Boolean): Result<Unit> {
        val path = "users/$userId/media_items/$itemId"
        return try {
            val docRef = db.collection("users").document(userId).collection("media_items").document(itemId)
            docRef.update(
                mapOf(
                    "isFavorite" to isFavorite,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // Create a playlist
    suspend fun createPlaylist(userId: String, playlist: Playlist): Result<String> {
        val path = "users/$userId/playlists"
        return try {
            val collectionRef = db.collection("users").document(userId).collection("playlists")
            val docRef = if (playlist.id.isNotBlank() && playlist.id.matches(Regex("^[a-zA-Z0-9_\\-]+$"))) {
                collectionRef.document(playlist.id)
            } else {
                collectionRef.document()
            }

            val payload = mutableMapOf<String, Any>(
                "userId" to userId,
                "name" to playlist.name.trim(),
                "itemCount" to playlist.itemCount,
                "color" to (playlist.color ?: "#6366F1"),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            playlist.description?.takeIf { it.isNotBlank() }?.let { payload["description"] = it.trim() }
            playlist.coverUrl?.takeIf { it.isNotBlank() }?.let { payload["coverUrl"] = it.trim() }

            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // Update playlist details
    suspend fun updatePlaylist(userId: String, playlist: Playlist): Result<Unit> {
        val path = "users/$userId/playlists/${playlist.id}"
        return try {
            val docRef = db.collection("users").document(userId).collection("playlists").document(playlist.id)
            val payload = mutableMapOf<String, Any>(
                "name" to playlist.name.trim(),
                "itemCount" to playlist.itemCount,
                "color" to (playlist.color ?: "#6366F1"),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            playlist.description?.takeIf { it.isNotBlank() }?.let { payload["description"] = it.trim() }
            playlist.coverUrl?.takeIf { it.isNotBlank() }?.let { payload["coverUrl"] = it.trim() }

            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // Delete a playlist
    suspend fun deletePlaylist(userId: String, playlistId: String): Result<Unit> {
        val path = "users/$userId/playlists/$playlistId"
        return try {
            db.collection("users").document(userId).collection("playlists").document(playlistId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }
}
