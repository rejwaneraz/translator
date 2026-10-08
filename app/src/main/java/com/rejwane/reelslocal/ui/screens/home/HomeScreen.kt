package com.rejwane.reelslocal.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rejwane.reelslocal.R
import com.rejwane.reelslocal.ui.screens.comments.CommentSheet
import com.rejwane.reelslocal.ui.screens.comments.CommentSheetViewModel

/**
 * Home: dark feed surface with compact "For You | Following" top tabs, a search entry, and the
 * full-screen vertical video pager. Comment sheet and profile navigation are wired in later phases.
 */
@Composable
fun HomeScreen(
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenProfile: (Long) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    commentViewModel: CommentSheetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val muted by viewModel.muted.collectAsStateWithLifecycle()
    val commentVideoId by commentViewModel.videoId.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HomeTopTabs(
            selectedTab = state.selectedTab,
            onTabSelected = viewModel::selectTab,
            onOpenSearch = onOpenSearch
        )

        when {
            state.isLoading -> {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        color = Color.White.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            state.videos.isEmpty() -> {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(
                            if (state.selectedTab == HomeTab.FOLLOWING) {
                                R.string.feed_following_empty
                            } else {
                                R.string.feed_empty
                            }
                        ),
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                VideoFeedPager(
                    videos = state.videos,
                    interactions = viewModel,
                    muted = muted,
                    onComment = { videoId -> commentViewModel.open(videoId) },
                    onOpenProfile = onOpenProfile,
                    onToggleFollow = viewModel::toggleFollow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }

    if (commentVideoId != null) {
        CommentSheet(
            viewModel = commentViewModel,
            onDismiss = { commentViewModel.close() }
        )
    }
}

@Composable
private fun HomeTopTabs(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    onOpenSearch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.weight(1f))
        HomeTabLabel(
            text = stringResource(R.string.home_for_you),
            selected = selectedTab == HomeTab.FOR_YOU,
            onClick = { onTabSelected(HomeTab.FOR_YOU) }
        )
        Spacer(Modifier.width(20.dp))
        HomeTabLabel(
            text = stringResource(R.string.home_following),
            selected = selectedTab == HomeTab.FOLLOWING,
            onClick = { onTabSelected(HomeTab.FOLLOWING) }
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onOpenSearch) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = stringResource(R.string.action_search),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun HomeTabLabel(text: String, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Text(
        text = text,
        color = if (selected) Color.White else Color.White.copy(alpha = 0.55f),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 4.dp, vertical = 6.dp)
    )
}
