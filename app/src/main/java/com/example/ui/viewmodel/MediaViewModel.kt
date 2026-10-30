package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MediaRepository
import com.example.model.MediaItem
import com.example.model.MediaType
import com.example.model.Playlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class FilterCategory(val label: String, val typeKey: String?) {
    ALL("All", null),
    SONGS("Songs", "song"),
    YOUTUBE("YouTube", "youtube"),
    PLAYLISTS("Playlists", null),
    VIDEOS("Videos", "video"),
    AUDIO("Audio", "audio"),
    PDFS("PDFs", "pdf"),
    PHOTOS("Photos", "photo"),
    NOTES("Notes", "note"),
    FAVORITES("Favorites", null)
}

class MediaViewModel(
    private val repository: MediaRepository,
    val currentUserId: String
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(FilterCategory.ALL)
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist = _selectedPlaylist.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage = _userMessage.asStateFlow()

    // Dialog & Detail states
    private val _isAddEditDialogOpen = MutableStateFlow(false)
    val isAddEditDialogOpen = _isAddEditDialogOpen.asStateFlow()

    private val _editingMediaItem = MutableStateFlow<MediaItem?>(null)
    val editingMediaItem = _editingMediaItem.asStateFlow()

    private val _selectedMediaDetail = MutableStateFlow<MediaItem?>(null)
    val selectedMediaDetail = _selectedMediaDetail.asStateFlow()

    private val _isAddPlaylistDialogOpen = MutableStateFlow(false)
    val isAddPlaylistDialogOpen = _isAddPlaylistDialogOpen.asStateFlow()

    private val _editingPlaylist = MutableStateFlow<Playlist?>(null)
    val editingPlaylist = _editingPlaylist.asStateFlow()

    // Realtime state for media items
    val mediaItemsState: StateFlow<UiState<List<MediaItem>>> = repository
        .observeMediaItems(currentUserId)
        .map<List<MediaItem>, UiState<List<MediaItem>>> { UiState.Success(it) }
        .catch { error ->
            Log.e(TAG, "Failed to observe media items", error)
            emit(UiState.Error(error.localizedMessage ?: "Failed to load media items"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    // Realtime state for playlists
    val playlistsState: StateFlow<UiState<List<Playlist>>> = repository
        .observePlaylists(currentUserId)
        .map<List<Playlist>, UiState<List<Playlist>>> { UiState.Success(it) }
        .catch { error ->
            Log.e(TAG, "Failed to observe playlists", error)
            emit(UiState.Error(error.localizedMessage ?: "Failed to load playlists"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    // Filtered media items state combining items, search query, selected filter and playlist
    val filteredMediaItems: StateFlow<List<MediaItem>> = combine(
        mediaItemsState,
        _searchQuery,
        _selectedFilter,
        _selectedPlaylist
    ) { itemsState, query, filter, activePlaylist ->
        if (itemsState !is UiState.Success) return@combine emptyList()
        var list = itemsState.data

        // Filter by active playlist if viewing a specific playlist
        if (activePlaylist != null) {
            list = list.filter { it.playlistIds.contains(activePlaylist.id) }
        } else {
            // Apply category filter
            when (filter) {
                FilterCategory.ALL -> Unit
                FilterCategory.PLAYLISTS -> Unit
                FilterCategory.FAVORITES -> list = list.filter { it.isFavorite }
                else -> filter.typeKey?.let { key ->
                    list = list.filter { it.type.equals(key, ignoreCase = true) }
                }
            }
        }

        // Apply search query
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter { item ->
                item.title.lowercase().contains(q) ||
                (item.artistOrAuthor?.lowercase()?.contains(q) == true) ||
                (item.content?.lowercase()?.contains(q) == true) ||
                (item.category?.lowercase()?.contains(q) == true) ||
                item.tags.any { it.lowercase().contains(q) }
            }
        }

        list.sortedByDescending { it.createdAt?.seconds ?: 0L }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: FilterCategory) {
        _selectedFilter.value = filter
        if (filter != FilterCategory.PLAYLISTS) {
            _selectedPlaylist.value = null
        }
    }

    fun onPlaylistClicked(playlist: Playlist?) {
        _selectedPlaylist.value = playlist
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun openAddMediaDialog(itemToEdit: MediaItem? = null) {
        _editingMediaItem.value = itemToEdit
        _isAddEditDialogOpen.value = true
    }

    fun closeAddMediaDialog() {
        _isAddEditDialogOpen.value = false
        _editingMediaItem.value = null
    }

    fun openMediaDetail(item: MediaItem) {
        _selectedMediaDetail.value = item
    }

    fun closeMediaDetail() {
        _selectedMediaDetail.value = null
    }

    fun openAddPlaylistDialog(playlistToEdit: Playlist? = null) {
        _editingPlaylist.value = playlistToEdit
        _isAddPlaylistDialogOpen.value = true
    }

    fun closeAddPlaylistDialog() {
        _isAddPlaylistDialogOpen.value = false
        _editingPlaylist.value = null
    }

    fun saveMediaItem(
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
    ) {
        viewModelScope.launch {
            val currentItem = _editingMediaItem.value
            if (currentItem != null && currentItem.id.isNotBlank()) {
                val updated = currentItem.copy(
                    title = title,
                    type = type,
                    url = url,
                    content = content,
                    artistOrAuthor = artistOrAuthor,
                    category = category,
                    tags = tags,
                    playlistIds = playlistIds,
                    thumbnailUrl = thumbnailUrl,
                    isFavorite = isFavorite
                )
                val result = repository.updateMediaItem(currentUserId, updated)
                if (result.isSuccess) {
                    _userMessage.value = "Updated \"$title\""
                    closeAddMediaDialog()
                    if (_selectedMediaDetail.value?.id == updated.id) {
                        _selectedMediaDetail.value = updated
                    }
                } else {
                    _userMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to update item"
                }
            } else {
                val newItem = MediaItem(
                    userId = currentUserId,
                    title = title,
                    type = type,
                    url = url,
                    content = content,
                    artistOrAuthor = artistOrAuthor,
                    category = category,
                    tags = tags,
                    playlistIds = playlistIds,
                    thumbnailUrl = thumbnailUrl,
                    isFavorite = isFavorite
                )
                val result = repository.addMediaItem(currentUserId, newItem)
                if (result.isSuccess) {
                    _userMessage.value = "Saved \"$title\""
                    closeAddMediaDialog()
                } else {
                    _userMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to save item"
                }
            }
        }
    }

    fun toggleFavorite(itemId: String, currentFavorite: Boolean) {
        viewModelScope.launch {
            val result = repository.toggleFavorite(currentUserId, itemId, !currentFavorite)
            if (result.isSuccess) {
                _selectedMediaDetail.value = _selectedMediaDetail.value?.let {
                    if (it.id == itemId) it.copy(isFavorite = !currentFavorite) else it
                }
            } else {
                _userMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to update favorite"
            }
        }
    }

    fun deleteMediaItem(itemId: String) {
        viewModelScope.launch {
            val result = repository.deleteMediaItem(currentUserId, itemId)
            if (result.isSuccess) {
                _userMessage.value = "Item removed"
                closeMediaDetail()
            } else {
                _userMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to delete item"
            }
        }
    }

    fun savePlaylist(name: String, description: String?, color: String) {
        viewModelScope.launch {
            val currentPlaylist = _editingPlaylist.value
            if (currentPlaylist != null && currentPlaylist.id.isNotBlank()) {
                val updated = currentPlaylist.copy(
                    name = name,
                    description = description,
                    color = color
                )
                val result = repository.updatePlaylist(currentUserId, updated)
                if (result.isSuccess) {
                    _userMessage.value = "Playlist updated"
                    closeAddPlaylistDialog()
                    if (_selectedPlaylist.value?.id == updated.id) {
                        _selectedPlaylist.value = updated
                    }
                } else {
                    _userMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to update playlist"
                }
            } else {
                val newPlaylist = Playlist(
                    userId = currentUserId,
                    name = name,
                    description = description,
                    color = color,
                    itemCount = 0L
                )
                val result = repository.createPlaylist(currentUserId, newPlaylist)
                if (result.isSuccess) {
                    _userMessage.value = "Playlist \"$name\" created"
                    closeAddPlaylistDialog()
                } else {
                    _userMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to create playlist"
                }
            }
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            val result = repository.deletePlaylist(currentUserId, playlistId)
            if (result.isSuccess) {
                _userMessage.value = "Playlist deleted"
                if (_selectedPlaylist.value?.id == playlistId) {
                    _selectedPlaylist.value = null
                }
            } else {
                _userMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to delete playlist"
            }
        }
    }

    fun toggleItemInPlaylist(item: MediaItem, playlistId: String) {
        viewModelScope.launch {
            val currentPlaylists = item.playlistIds.toMutableList()
            val isPresent = currentPlaylists.contains(playlistId)
            if (isPresent) {
                currentPlaylists.remove(playlistId)
            } else {
                currentPlaylists.add(playlistId)
            }
            val updated = item.copy(playlistIds = currentPlaylists)
            val result = repository.updateMediaItem(currentUserId, updated)
            if (result.isSuccess) {
                _selectedMediaDetail.value = updated
                _userMessage.value = if (isPresent) "Removed from playlist" else "Added to playlist"
            }
        }
    }

    private companion object {
        const val TAG = "MediaViewModel"
    }
}
