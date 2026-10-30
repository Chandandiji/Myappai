package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Playlist(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String? = null,
    val itemCount: Long = 0L,
    val coverUrl: String? = null,
    val color: String? = "#6366F1",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
