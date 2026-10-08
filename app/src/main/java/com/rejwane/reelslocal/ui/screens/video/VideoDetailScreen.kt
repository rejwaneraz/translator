package com.rejwane.reelslocal.ui.screens.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rejwane.reelslocal.R
import com.rejwane.reelslocal.ui.screens.comments.CommentSheet
import com.rejwane.reelslocal.ui.screens.comments.CommentSheetViewModel
import com.rejwane.reelslocal.ui.screens.home.VideoFeedPager

/**
 * Standalone full-screen video reached from Explore / Profile / Search. Reuses the Home feed pager
 * for a single video, adds a back affordance, and hosts its own comment sheet.
 */
@Composable
fun VideoDetailScreen(
    onBack: () -> Unit,
    onOpenProfile: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VideoDetailViewModel = hiltViewModel(),
    commentViewModel: CommentSheetViewModel = hiltViewModel()
) {
    val videos by viewModel.videos.collectAsStateWithLifecycle()
    val muted by viewModel.muted.collectAsStateWithLifecycle()
    val commentVideoId by commentViewModel.videoId.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (videos.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.video_unavailable),
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            VideoFeedPager(
                videos = videos,
                interactions = viewModel,
                muted = muted,
                onComment = { videoId -> commentViewModel.open(videoId) },
                onOpenProfile = onOpenProfile,
                onToggleFollow = viewModel::toggleFollow,
                modifier = Modifier.fillMaxSize()
            )
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 12.dp, start = 12.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                tint = Color.White
            )
        }
    }

    if (commentVideoId != null) {
        CommentSheet(
            viewModel = commentViewModel,
            onDismiss = { commentViewModel.close() }
        )
    }
}
