package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.model.MediaItem
import com.example.model.MediaType
import com.example.model.Playlist

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditMediaDialog(
    itemToEdit: MediaItem?,
    availablePlaylists: List<Playlist>,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        type: String,
        url: String?,
        content: String?,
        artistOrAuthor: String?,
        category: String?,
        tags: List<String>,
        playlistIds: List<String>,
        thumbnailUrl: String?,
        isFavorite: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf(itemToEdit?.title ?: "") }
    var selectedType by remember { mutableStateOf(MediaType.fromKey(itemToEdit?.type ?: "note")) }
    var url by remember { mutableStateOf(itemToEdit?.url ?: "") }
    var content by remember { mutableStateOf(itemToEdit?.content ?: "") }
    var artistOrAuthor by remember { mutableStateOf(itemToEdit?.artistOrAuthor ?: "") }
    var category by remember { mutableStateOf(itemToEdit?.category ?: "") }
    var thumbnailUrl by remember { mutableStateOf(itemToEdit?.thumbnailUrl ?: "") }
    var isFavorite by remember { mutableStateOf(itemToEdit?.isFavorite ?: false) }
    var tagInput by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(itemToEdit?.tags ?: emptyList()) }
    var selectedPlaylistIds by remember { mutableStateOf(itemToEdit?.playlistIds ?: emptyList()) }
    var titleError by remember { mutableStateOf(false) }

    // Auto-detect YouTube URL
    fun checkAutoDetectYoutube(inputUrl: String) {
        val trimmed = inputUrl.trim()
        val youtubeId = extractYouTubeVideoId(trimmed)
        if (youtubeId != null) {
            selectedType = MediaType.YOUTUBE
            if (thumbnailUrl.isBlank()) {
                thumbnailUrl = "https://img.youtube.com/vi/$youtubeId/hqdefault.jpg"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .padding(vertical = 24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (itemToEdit == null) "Add New Media" else "Edit Media Item",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Media Type Selector Tabs
                Text(
                    text = "Media Type",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MediaType.entries.forEach { type ->
                        val isSelected = selectedType == type
                        val color = getMediaTypeColor(type)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, color) else null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedType = type }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = getMediaTypeIcon(type),
                                    contentDescription = null,
                                    tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = type.displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text("Title *") },
                    placeholder = {
                        Text(
                            when (selectedType) {
                                MediaType.SONG -> "Song Title (e.g. Midnight City)"
                                MediaType.YOUTUBE -> "Video Title"
                                MediaType.PDF -> "Document / Book Name"
                                MediaType.PHOTO -> "Photo Description / Artwork Title"
                                MediaType.NOTE -> "Note Subject / Heading"
                                else -> "Title"
                            }
                        )
                    },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Title is required", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("media_title_input")
                )

                // Artist / Author / Channel field (for songs, youtube, pdf, video, audio)
                OutlinedTextField(
                    value = artistOrAuthor,
                    onValueChange = { artistOrAuthor = it },
                    label = {
                        Text(
                            when (selectedType) {
                                MediaType.SONG, MediaType.AUDIO -> "Artist / Performer"
                                MediaType.YOUTUBE, MediaType.VIDEO -> "Channel / Creator"
                                MediaType.PDF -> "Author / Publisher"
                                else -> "Author / Artist (Optional)"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("media_artist_input")
                )

                // URL / Link field
                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        checkAutoDetectYoutube(it)
                    },
                    label = { Text("URL / Link (YouTube, Audio, PDF, Web)") },
                    placeholder = { Text("https://...") },
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("media_url_input")
                )

                // Thumbnail / Cover image URL field
                if (selectedType != MediaType.NOTE) {
                    OutlinedTextField(
                        value = thumbnailUrl,
                        onValueChange = { thumbnailUrl = it },
                        label = { Text("Thumbnail / Cover Image URL (Optional)") },
                        placeholder = { Text("https://... image link") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Content / Notes field
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = {
                        Text(
                            if (selectedType == MediaType.NOTE) "Note Body / Text *"
                            else if (selectedType == MediaType.SONG) "Lyrics / Chords / Notes (Optional)"
                            else "Notes / Summary / Description (Optional)"
                        )
                    },
                    placeholder = { Text("Write details, thoughts, key points...") },
                    minLines = if (selectedType == MediaType.NOTE) 4 else 2,
                    maxLines = 8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("media_content_input")
                )

                // Tags Input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = tagInput,
                            onValueChange = { tagInput = it },
                            placeholder = { Text("e.g. study, rock, work") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                val clean = tagInput.trim().replace("#", "")
                                if (clean.isNotBlank() && !tags.contains(clean)) {
                                    tags = tags + clean
                                    tagInput = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add tag",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (tags.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            tags.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "#$tag",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove tag",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { tags = tags - tag }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Playlists inclusion
                if (availablePlaylists.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Add to Playlists",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        availablePlaylists.forEach { pl ->
                            val isChecked = selectedPlaylistIds.contains(pl.id)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedPlaylistIds = if (isChecked) {
                                            selectedPlaylistIds - pl.id
                                        } else {
                                            selectedPlaylistIds + pl.id
                                        }
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        selectedPlaylistIds = if (checked) {
                                            selectedPlaylistIds + pl.id
                                        } else {
                                            selectedPlaylistIds - pl.id
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = pl.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Favorite Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isFavorite = !isFavorite }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) Color(0xFFF43F5E) else MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isFavorite) "Marked as Favorite" else "Add to Favorites",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    onSave(
                        title.trim(),
                        selectedType.key,
                        url.trim().takeIf { it.isNotBlank() },
                        content.trim().takeIf { it.isNotBlank() },
                        artistOrAuthor.trim().takeIf { it.isNotBlank() },
                        category.trim().takeIf { it.isNotBlank() },
                        tags,
                        selectedPlaylistIds,
                        thumbnailUrl.trim().takeIf { it.isNotBlank() },
                        isFavorite
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_media_button")
            ) {
                Text(if (itemToEdit == null) "Save to Vault" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun extractYouTubeVideoId(url: String): String? {
    if (url.isBlank()) return null
    val regex = Regex("(?:youtu\\.be\\/|youtube\\.com\\/(?:embed\\/|v\\/|watch\\?v=|watch\\?.+&v=))([\\w-]{11})")
    val match = regex.find(url)
    return match?.groupValues?.getOrNull(1)
}
