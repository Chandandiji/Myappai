package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class MediaItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val type: String = "note",
    val url: String? = null,
    val content: String? = null,
    val artistOrAuthor: String? = null,
    val category: String? = null,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val playlistIds: List<String> = emptyList(),
    val thumbnailUrl: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
