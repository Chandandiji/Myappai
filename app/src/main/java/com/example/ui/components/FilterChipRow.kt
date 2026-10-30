package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.FilterCategory

@Composable
fun FilterChipRow(
    selectedFilter: FilterCategory,
    onFilterSelected: (FilterCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterCategory.entries.forEach { category ->
            val isSelected = selectedFilter == category
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(category) },
                label = { Text(category.label) },
                leadingIcon = {
                    getCategoryIcon(category)?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("filter_chip_${category.name.lowercase()}")
            )
        }
    }
}

fun getCategoryIcon(category: FilterCategory): ImageVector? {
    return when (category) {
        FilterCategory.ALL -> null
        FilterCategory.SONGS -> Icons.Default.Audiotrack
        FilterCategory.YOUTUBE -> Icons.Default.PlayCircle
        FilterCategory.PLAYLISTS -> Icons.Default.Folder
        FilterCategory.VIDEOS -> Icons.Default.Videocam
        FilterCategory.AUDIO -> Icons.Default.Audiotrack
        FilterCategory.PDFS -> Icons.Default.Description
        FilterCategory.PHOTOS -> Icons.Default.Image
        FilterCategory.NOTES -> Icons.Default.Note
        FilterCategory.FAVORITES -> Icons.Default.Favorite
    }
}
