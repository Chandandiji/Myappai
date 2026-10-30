package com.example.model

enum class MediaType(val key: String, val displayName: String) {
    SONG("song", "Songs"),
    YOUTUBE("youtube", "YouTube"),
    VIDEO("video", "Videos"),
    AUDIO("audio", "Audio"),
    PDF("pdf", "PDFs"),
    PHOTO("photo", "Photos"),
    NOTE("note", "Notes");

    companion object {
        fun fromKey(key: String): MediaType = entries.find { it.key.equals(key, ignoreCase = true) } ?: NOTE
    }
}
