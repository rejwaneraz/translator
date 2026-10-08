@file:OptIn(UnstableApi::class)

package com.rejwane.reelslocal.ui.screens.home

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.VideocamOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import androidx.media3.common.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.rejwane.reelslocal.R
import com.rejwane.reelslocal.data.database.relation.VideoWithOwner
import com.rejwane.reelslocal.ui.components.ActionRail
import com.rejwane.reelslocal.ui.components.UserAvatar
import com.rejwane.reelslocal.ui.player.VideoPlayerSurface
import com.rejwane.reelslocal.ui.screens.feed.FeedInteractions
import com.rejwane.reelslocal.utils.MediaPaths
import com.rejwane.reelslocal.utils.formatCount
import kotlinx.coroutines.flow.flowOf

/**
 * A single full-screen video page in the vertical feed: poster thumbnail, live player (only when
 * this page is active), tap-to-pause / double-tap-to-like gestures, bottom caption overlay and the
 * right-hand action rail. Degrades to a friendly placeholder when the media file is missing.
 */
@Composable
fun FeedVideoPage(
    item: VideoWithOwner,
    player: ExoPlayer,
    isActive: Boolean,
    muted: Boolean,
    interactions: FeedInteractions,
    onComment: (Long) -> Unit,
    onOpenProfile: (Long) -> Unit,
    onToggleFollow: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val video = item.video
    val owner = item.owner

    val isLiked by remember(video.id) { interactions.observeIsLiked(video.id) }
        .collectAsStateWithLifecycle(initialValue = false)
    val isSaved by remember(video.id) { interactions.observeIsSaved(video.id) }
        .collectAsStateWithLifecycle(initialValue = false)
    val isFollowingOwner by remember(owner?.id) {
        owner?.id?.let { interactions.observeIsFollowingOwner(it) } ?: flowOf(false)
    }.collectAsStateWithLifecycle(initialValue = false)

    var playing by remember { mutableStateOf(false) }
    var buffering by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    val videoPath = video.videoPath

    // Load / play when this page becomes active; pause when it leaves.
    LaunchedEffect(isActive, videoPath) {
        if (isActive) {
            hasError = false
            if (videoPath.isBlank()) {
                hasError = true
            } else {
                if (player.currentMediaItem?.mediaId != videoPath) {
                    val mediaItem = MediaItem.Builder()
                        .setUri(MediaPaths.videoUri(videoPath))
                        .setMediaId(videoPath)
                        .build()
                    player.setMediaItem(mediaItem)
                    player.prepare()
                }
                player.playWhenReady = true
            }
        } else {
            if (player.currentMediaItem?.mediaId == videoPath) {
                player.playWhenReady = false
            }
        }
    }

    LaunchedEffect(muted, player) {
        player.volume = if (muted) 0f else 1f
    }

    // Observe playback only while active.
    LaunchedEffect(isActive, player) {
        if (!isActive) return@LaunchedEffect
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playing = isPlaying
            }

            override fun onPlaybackStateChanged(state: Int) {
                buffering = state == Player.STATE_BUFFERING
            }

            override fun onPlayerError(error: PlaybackException) {
                hasError = true
                buffering = false
                playing = false
            }
        }
        player.addListener(listener)
        playing = player.isPlayingNow
        onDispose { player.removeListener(listener) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(video.id, isActive) {
                detectTapGestures(
                    onTap = {
                        if (isActive && !hasError) player.playWhenReady = !player.playWhenReady
                    },
                    onDoubleTap = {
                        if (!isLiked) interactions.toggleLike(video.id)
                    }
                )
            }
    ) {
        // Poster thumbnail — always present so the page is never a blank black rectangle.
        AsyncImage(
            model = MediaPaths.imageModel(video.thumbnailPath),
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (isActive && !hasError) {
            VideoPlayerSurface(player = player, modifier = Modifier.fillMaxSize())
        }

        when {
            hasError -> VideoUnavailablePlaceholder(Modifier.fillMaxSize())
            buffering -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
            isActive && !playing -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = stringResource(R.string.action_play),
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(72.dp)
                )
            }
        }

        // Mute / unmute toggle.
        IconButton(
            onClick = { interactions.toggleMute() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f))
        ) {
            Icon(
                imageVector = if (muted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                contentDescription = stringResource(
                    if (muted) R.string.action_unmute else R.string.action_mute
                ),
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        // Bottom readability scrim.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                    )
                )
        )

        VideoInfoOverlay(
            item = item,
            isFollowingOwner = isFollowingOwner,
            onOpenProfile = onOpenProfile,
            onToggleFollow = onToggleFollow,
            modifier = Modifier.align(Alignment.BottomStart)
        )

        ActionRail(
            isLiked = isLiked,
            likeCount = video.likesCount,
            onLike = { interactions.toggleLike(video.id) },
            commentCount = video.commentsCount,
            onComment = { onComment(video.id) },
            shareCount = video.sharesCount,
            onShare = { interactions.share(video.id) },
            isSaved = isSaved,
            onSave = { interactions.toggleSave(video.id) },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp, bottom = 40.dp)
        )
    }
}

@Composable
private fun VideoUnavailablePlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Color(0xFF101014)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.VideocamOff,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.video_unavailable),
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun VideoInfoOverlay(
    item: VideoWithOwner,
    isFollowingOwner: Boolean,
    onOpenProfile: (Long) -> Unit,
    onToggleFollow: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val video = item.video
    val owner = item.owner
    val ownerId = owner?.id
    val isOwnVideo = ownerId == null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 84.dp, bottom = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .then(
                        if (ownerId != null) {
                            Modifier.pointerInput(ownerId) {
                                detectTapGestures(onTap = { onOpenProfile(ownerId) })
                            }
                        } else Modifier
                    )
            ) {
                UserAvatar(user = owner, size = 40.dp)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = owner?.displayName ?: "?",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = owner?.let { "@${it.username}" } ?: "",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!isOwnVideo && ownerId != null) {
                FollowPill(
                    following = isFollowingOwner,
                    onClick = { onToggleFollow(ownerId) }
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        if (video.caption.isNotBlank()) {
            Text(
                text = video.caption,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
        }

        val tags = video.hashtags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (tags.isNotEmpty()) {
            Text(
                text = tags.joinToString(" ") { "#$it" },
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.MusicNote,
                contentDescription = stringResource(R.string.music_note),
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = video.musicTitle.ifBlank { "Original audio" },
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "▶ ${formatCount(video.views)} ${stringResource(R.string.views_suffix)}",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun FollowPill(following: Boolean, onClick: () -> Unit) {
    val bg = if (following) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .pointerInput(following) { detectTapGestures(onTap = { onClick() }) }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = stringResource(if (following) R.string.following else R.string.follow),
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private val ExoPlayer.isPlayingNow: Boolean
    get() = playWhenReady && playbackState == Player.STATE_READY
