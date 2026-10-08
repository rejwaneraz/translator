package com.rejwane.reelslocal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rejwane.reelslocal.data.database.entity.User
import com.rejwane.reelslocal.utils.MediaPaths

/**
 * Avatar that never crashes on missing/corrupt images: initials are always drawn
 * underneath, and the Coil layer simply stays transparent when loading fails.
 */
@Composable
fun UserAvatar(
    user: User?,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val initials = user?.displayName
        ?.trim()
        ?.take(1)
        ?.uppercase()
        ?.takeIf { it.isNotEmpty() }
        ?: "?"
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        AsyncImage(
            model = MediaPaths.imageModel(user?.avatarPath),
            contentDescription = user?.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
        )
    }
}
