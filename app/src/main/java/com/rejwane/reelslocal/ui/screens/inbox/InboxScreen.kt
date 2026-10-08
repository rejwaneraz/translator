package com.rejwane.reelslocal.ui.screens.inbox

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rejwane.reelslocal.R
import com.rejwane.reelslocal.data.database.relation.ActivityDetails
import com.rejwane.reelslocal.data.model.ActivityType
import com.rejwane.reelslocal.utils.formatRelativeTime

/**
 * Inbox == the local activity list. Opening it marks everything read (clearing the nav badge);
 * each row navigates to the relevant video or profile.
 */
@Composable
fun InboxScreen(
    onOpenVideo: (Long) -> Unit,
    onOpenUser: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val activities by viewModel.activities.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.markAllRead() }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.inbox_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        if (activities.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.inbox_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(activities, key = { it.activity.id }) { details ->
                    ActivityRow(
                        details = details,
                        onClick = {
                            val videoId = details.activity.targetVideoId
                            val userId = details.activity.targetUserId
                            when {
                                videoId != null -> onOpenVideo(videoId)
                                userId != null -> onOpenUser(userId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(details: ActivityDetails, onClick: () -> Unit) {
    val activity = details.activity
    val type = runCatching { ActivityType.valueOf(activity.type) }.getOrNull()
    val (icon, tint) = iconFor(type)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = activity.message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (activity.isRead) FontWeight.Normal else FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = formatRelativeTime(activity.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!activity.isRead) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

private fun iconFor(type: ActivityType?): Pair<ImageVector, Color> = when (type) {
    ActivityType.LIKE_VIDEO, ActivityType.LIKE_COMMENT ->
        Icons.Filled.Favorite to Color(0xFFFF2E63)
    ActivityType.COMMENT_VIDEO ->
        Icons.Filled.ChatBubble to Color(0xFF08D9D6)
    ActivityType.FOLLOW_USER ->
        Icons.Filled.PersonAdd to Color(0xFF4C8DF6)
    ActivityType.SAVE_VIDEO ->
        Icons.Filled.Bookmark to Color(0xFFFFC94D)
    ActivityType.SHARE_VIDEO ->
        Icons.Filled.Share to Color(0xFF7E57C2)
    ActivityType.VIDEO_ADDED ->
        Icons.Filled.VideoLibrary to Color(0xFF26A69A)
    ActivityType.PROFILE_UPDATE ->
        Icons.Filled.Edit to Color(0xFF8D8D99)
    null -> Icons.Filled.Favorite to Color(0xFF8D8D99)
}
