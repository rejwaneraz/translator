package com.rejwane.reelslocal.ui.screens.profile

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rejwane.reelslocal.R
import com.rejwane.reelslocal.data.database.entity.User
import com.rejwane.reelslocal.data.database.entity.Video
import com.rejwane.reelslocal.ui.components.UserAvatar
import com.rejwane.reelslocal.ui.components.VideoGridItem
import com.rejwane.reelslocal.utils.formatCount

/**
 * Full social profile: header (avatar, stats, bio), Edit/Follow+Say hi actions, and a tabbed
 * grid (Videos / Liked / Saved) or Following list. Back arrow only shows on the detail route.
 */
@Composable
fun ProfileScreen(
    onOpenVideo: (Long) -> Unit,
    onOpenUser: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onEditProfile: () -> Unit = {},
    onSayHi: (Long) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item(span = { GridItemSpan(3) }) {
            ProfileHeader(
                state = state,
                onBack = onBack,
                onFollow = viewModel::toggleFollow,
                onEditProfile = onEditProfile,
                onSayHi = onSayHi
            )
        }
        item(span = { GridItemSpan(3) }) {
            ProfileTabs(selected = state.selectedTab, onSelect = viewModel::selectTab)
        }

        when (val content = state.content) {
            is ProfileContent.Videos -> {
                if (content.items.isEmpty()) {
                    item(span = { GridItemSpan(3) }) { ContentHint(stringResource(R.string.feed_empty)) }
                } else {
                    items(content.items.size) { index ->
                        val video: Video = content.items[index]
                        VideoGridItem(
                            thumbnailPath = video.thumbnailPath,
                            views = video.views,
                            durationMs = video.durationMs,
                            contentDescription = video.title,
                            onClick = { onOpenVideo(video.id) }
                        )
                    }
                }
            }
            is ProfileContent.Users -> {
                if (content.items.isEmpty()) {
                    item(span = { GridItemSpan(3) }) { ContentHint(stringResource(R.string.feed_following_empty)) }
                } else {
                    items(content.items.size, span = { GridItemSpan(3) }) { index ->
                        val user: User = content.items[index]
                        FollowingRow(user = user, onClick = { onOpenUser(user.id) })
                    }
                }
            }
            ProfileContent.Empty -> Unit
        }
    }
}

@Composable
private fun ProfileHeader(
    state: ProfileUiState,
    onBack: (() -> Unit)?,
    onFollow: () -> Unit,
    onEditProfile: () -> Unit,
    onSayHi: (Long) -> Unit
) {
    val user = state.user
    Column(modifier = Modifier.fillMaxWidth()) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.padding(start = 4.dp, top = 4.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(user = user, size = 84.dp)
            Spacer(Modifier.width(20.dp))
            Row(modifier = Modifier.weight(1f)) {
                StatColumn(formatCount(user?.followersCount ?: 0), stringResource(R.string.stat_followers))
                StatColumn(formatCount(user?.followingCount ?: 0), stringResource(R.string.stat_following))
                StatColumn(formatCount(user?.likesCount ?: 0), stringResource(R.string.stat_likes))
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user?.displayName ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (user?.isVerified == true) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = stringResource(R.string.verified),
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = user?.let { "@${it.username}" } ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!user?.bio.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = user?.bio ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (state.isCurrentUser) {
                Button(
                    onClick = onEditProfile,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(R.string.edit_profile))
                }
            } else {
                Button(
                    onClick = onFollow,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = if (state.isFollowing) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Text(stringResource(if (state.isFollowing) R.string.following else R.string.follow))
                }
                OutlinedButton(
                    onClick = { user?.id?.let(onSayHi) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(R.string.say_hi))
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun StatColumn(value: String, label: String) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileTabs(selected: ProfileTab, onSelect: (ProfileTab) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        ProfileTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .clickable { onSelect(tab) }
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(tab.labelRes()),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(3.dp)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                )
            }
        }
    }
}

private fun ProfileTab.labelRes(): Int = when (this) {
    ProfileTab.VIDEOS -> R.string.tab_videos
    ProfileTab.LIKED -> R.string.tab_liked
    ProfileTab.SAVED -> R.string.tab_saved
    ProfileTab.FOLLOWING -> R.string.tab_following
}

@Composable
private fun FollowingRow(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(user = user, size = 44.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ContentHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}
