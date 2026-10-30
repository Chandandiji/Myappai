package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Playlist
import com.example.ui.components.AddEditMediaDialog
import com.example.ui.components.AddEditPlaylistDialog
import com.example.ui.components.FilterChipRow
import com.example.ui.components.MediaCard
import com.example.ui.components.MediaDetailSheet
import com.example.ui.components.PlaylistCard
import com.example.ui.viewmodel.FilterCategory
import com.example.ui.viewmodel.MediaViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MediaViewModel,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()
    val mediaItemsState by viewModel.mediaItemsState.collectAsStateWithLifecycle()
    val playlistsState by viewModel.playlistsState.collectAsStateWithLifecycle()
    val filteredItems by viewModel.filteredMediaItems.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val isAddEditDialogOpen by viewModel.isAddEditDialogOpen.collectAsStateWithLifecycle()
    val editingMediaItem by viewModel.editingMediaItem.collectAsStateWithLifecycle()
    val selectedMediaDetail by viewModel.selectedMediaDetail.collectAsStateWithLifecycle()

    val isAddPlaylistDialogOpen by viewModel.isAddPlaylistDialogOpen.collectAsStateWithLifecycle()
    val editingPlaylist by viewModel.editingPlaylist.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var isSearchActive by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var fabMenuExpanded by remember { mutableStateOf(false) }

    val allPlaylists = (playlistsState as? UiState.Success)?.data ?: emptyList()
    val currentUser = Firebase.auth.currentUser

    // Handle back button when drilled into a playlist or search
    BackHandler(enabled = selectedPlaylist != null || isSearchActive) {
        if (selectedPlaylist != null) {
            viewModel.onPlaylistClicked(null)
        } else if (isSearchActive) {
            isSearchActive = false
            viewModel.onSearchQueryChange("")
        }
    }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    if (selectedPlaylist != null) {
                        Column {
                            Text(
                                text = selectedPlaylist!!.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Playlist Collection",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = { Text("Search title, artist, notes, #tag...") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_text_input")
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF6366F1), Color(0xFF06B6D4))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "MediaVault",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (selectedPlaylist != null) {
                        IconButton(onClick = { viewModel.onPlaylistClicked(null) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to library")
                        }
                    }
                },
                actions = {
                    if (selectedPlaylist == null) {
                        IconButton(
                            onClick = {
                                isSearchActive = !isSearchActive
                                if (!isSearchActive) viewModel.onSearchQueryChange("")
                            },
                            modifier = Modifier.testTag("search_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (isSearchActive) "Close search" else "Search media"
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.testTag("user_menu_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser?.displayName?.take(1)?.uppercase() ?: "U",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = currentUser?.displayName ?: "User",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = currentUser?.email ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {}
                            )
                            DropdownMenuItem(
                                text = { Text("Sign Out", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Logout,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onSignOut()
                                },
                                modifier = Modifier.testTag("sign_out_button")
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.navigationBarsPadding()
            ) {
                AnimatedVisibility(
                    visible = fabMenuExpanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                fabMenuExpanded = false
                                viewModel.openAddPlaylistDialog()
                            },
                            icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                            text = { Text("New Playlist") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.testTag("create_playlist_fab")
                        )
                        ExtendedFloatingActionButton(
                            onClick = {
                                fabMenuExpanded = false
                                viewModel.openAddMediaDialog()
                            },
                            icon = { Icon(Icons.Default.Add, contentDescription = null) },
                            text = { Text("Add Media") },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.testTag("add_media_fab")
                        )
                    }
                }

                FloatingActionButton(
                    onClick = {
                        if (fabMenuExpanded) {
                            fabMenuExpanded = false
                        } else {
                            viewModel.openAddMediaDialog()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("main_fab")
                ) {
                    Icon(
                        imageVector = if (fabMenuExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Add"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category Filter row (only visible when not drilled into a specific playlist)
            if (selectedPlaylist == null) {
                FilterChipRow(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { viewModel.onFilterSelected(it) }
                )
            } else {
                // Active Playlist Header banner
                PlaylistHeaderBanner(
                    playlist = selectedPlaylist!!,
                    itemCount = filteredItems.size,
                    onBack = { viewModel.onPlaylistClicked(null) }
                )
            }

            // Main Content Area
            when {
                mediaItemsState is UiState.Loading || playlistsState is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                selectedFilter == FilterCategory.PLAYLISTS && selectedPlaylist == null -> {
                    // Playlists View
                    val playlists = (playlistsState as? UiState.Success)?.data ?: emptyList()
                    val allItems = (mediaItemsState as? UiState.Success)?.data ?: emptyList()

                    if (playlists.isEmpty()) {
                        EmptyPlaylistsView(
                            onCreatePlaylist = { viewModel.openAddPlaylistDialog() }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(playlists, key = { it.id }) { playlist ->
                                val count = allItems.count { it.playlistIds.contains(playlist.id) }
                                PlaylistCard(
                                    playlist = playlist,
                                    itemCount = count,
                                    onClick = { viewModel.onPlaylistClicked(playlist) },
                                    onEdit = { viewModel.openAddPlaylistDialog(playlist) },
                                    onDelete = { viewModel.deletePlaylist(playlist.id) }
                                )
                            }
                        }
                    }
                }

                filteredItems.isEmpty() -> {
                    EmptyMediaView(
                        filter = selectedFilter,
                        inPlaylist = selectedPlaylist != null,
                        searchQuery = searchQuery,
                        onAddMedia = { viewModel.openAddMediaDialog() }
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            MediaCard(
                                item = item,
                                onClick = { viewModel.openMediaDetail(item) },
                                onFavoriteToggle = { viewModel.toggleFavorite(item.id, item.isFavorite) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Media Dialog
    if (isAddEditDialogOpen) {
        AddEditMediaDialog(
            itemToEdit = editingMediaItem,
            availablePlaylists = allPlaylists,
            onDismiss = { viewModel.closeAddMediaDialog() },
            onSave = { title, type, url, content, artist, category, tags, playlistIds, thumb, fav ->
                viewModel.saveMediaItem(
                    title = title,
                    type = type,
                    url = url,
                    content = content,
                    artistOrAuthor = artist,
                    category = category,
                    tags = tags,
                    playlistIds = playlistIds,
                    thumbnailUrl = thumb,
                    isFavorite = fav
                )
            }
        )
    }

    // Add / Edit Playlist Dialog
    if (isAddPlaylistDialogOpen) {
        AddEditPlaylistDialog(
            playlistToEdit = editingPlaylist,
            onDismiss = { viewModel.closeAddPlaylistDialog() },
            onSave = { name, desc, color ->
                viewModel.savePlaylist(name, desc, color)
            }
        )
    }

    // Media Detail Sheet
    selectedMediaDetail?.let { item ->
        MediaDetailSheet(
            item = item,
            allPlaylists = allPlaylists,
            onDismiss = { viewModel.closeMediaDetail() },
            onEdit = {
                viewModel.closeMediaDetail()
                viewModel.openAddMediaDialog(item)
            },
            onDelete = { viewModel.deleteMediaItem(item.id) },
            onToggleFavorite = { viewModel.toggleFavorite(item.id, item.isFavorite) },
            onTogglePlaylist = { playlistId -> viewModel.toggleItemInPlaylist(item, playlistId) }
        )
    }
}

@Composable
fun PlaylistHeaderBanner(
    playlist: Playlist,
    itemCount: Int,
    onBack: () -> Unit
) {
    val baseColor = try {
        Color(android.graphics.Color.parseColor(playlist.color ?: "#6366F1"))
    } catch (e: Exception) {
        Color(0xFF6366F1)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = baseColor.copy(alpha = 0.15f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = baseColor.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = "$itemCount items",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = baseColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            if (!playlist.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = playlist.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyMediaView(
    filter: FilterCategory,
    inPlaylist: Boolean,
    searchQuery: String,
    onAddMedia: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.VideoLibrary,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (searchQuery.isNotBlank()) "No matching results"
            else if (inPlaylist) "This playlist is empty"
            else if (filter == FilterCategory.FAVORITES) "No favorites yet"
            else "Your media vault is waiting",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (searchQuery.isNotBlank()) "Try searching for a different keyword, artist, or tag"
            else if (inPlaylist) "Add songs, YouTube videos, or documents to this playlist"
            else if (filter == FilterCategory.FAVORITES) "Tap the heart icon on any media item to bookmark it here"
            else "Save your favorite songs, YouTube clips, PDFs, audio, photos, and notes in one organized place.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (searchQuery.isBlank() && filter != FilterCategory.FAVORITES) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAddMedia,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("empty_state_add_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add First Item")
            }
        }
    }
}

@Composable
fun EmptyPlaylistsView(
    onCreatePlaylist: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "No playlists yet",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create custom playlists to group and organize your songs, YouTube videos, and documents.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCreatePlaylist,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.testTag("empty_state_create_playlist_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Playlist")
        }
    }
}
