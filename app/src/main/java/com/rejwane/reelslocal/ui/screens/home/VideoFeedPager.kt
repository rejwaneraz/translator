@file:OptIn(UnstableApi::class)

package com.rejwane.reelslocal.ui.screens.home

import androidx.annotation.OptIn
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import com.rejwane.reelslocal.data.database.relation.VideoWithOwner
import com.rejwane.reelslocal.ui.player.rememberReelsPlayer
import com.rejwane.reelslocal.ui.screens.feed.FeedInteractions

/**
 * TikTok-style vertical feed. A single shared ExoPlayer (see [rememberReelsPlayer]) is handed to
 * every page, and only the settled page renders/loads it, so exactly one video decodes at a time.
 */
@Composable
fun VideoFeedPager(
    videos: List<VideoWithOwner>,
    interactions: FeedInteractions,
    muted: Boolean,
    onComment: (Long) -> Unit,
    onOpenProfile: (Long) -> Unit,
    onToggleFollow: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val player = rememberReelsPlayer()
    val pagerState = rememberPagerState(pageCount = { videos.size })

    VerticalPager(
        state = pagerState,
        modifier = modifier
    ) { page ->
        val item = videos.getOrNull(page) ?: return@VerticalPager
        FeedVideoPage(
            item = item,
            player = player,
            isActive = pagerState.currentPage == page,
            muted = muted,
            interactions = interactions,
            onComment = onComment,
            onOpenProfile = onOpenProfile,
            onToggleFollow = onToggleFollow
        )
    }
}
