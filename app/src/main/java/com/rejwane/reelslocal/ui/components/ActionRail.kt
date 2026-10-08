package com.rejwane.reelslocal.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rejwane.reelslocal.R
import com.rejwane.reelslocal.utils.formatCount

/**
 * Vertical action rail shown on the right edge of a video: like / comment / share / save,
 * each with its local count. Colors are fixed white so they read on any video background.
 */
@Composable
fun ActionRail(
    isLiked: Boolean,
    likeCount: Long,
    onLike: () -> Unit,
    commentCount: Long,
    onComment: () -> Unit,
    shareCount: Long,
    onShare: () -> Unit,
    isSaved: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        RailAction(
            icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            tint = if (isLiked) MaterialTheme.colorScheme.primary else Color.White,
            label = formatCount(likeCount),
            contentDescription = stringResource(R.string.action_like),
            onClick = onLike
        )
        RailAction(
            icon = Icons.Outlined.ChatBubbleOutline,
            tint = Color.White,
            label = formatCount(commentCount),
            contentDescription = stringResource(R.string.action_comment),
            onClick = onComment
        )
        RailAction(
            icon = Icons.Outlined.Share,
            tint = Color.White,
            label = formatCount(shareCount),
            contentDescription = stringResource(R.string.action_share),
            onClick = onShare
        )
        RailAction(
            icon = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            tint = if (isSaved) Color(0xFFFFC94D) else Color.White,
            label = null,
            contentDescription = stringResource(R.string.action_save),
            onClick = onSave
        )
    }
}

@Composable
private fun RailAction(
    icon: ImageVector,
    tint: Color,
    label: String?,
    contentDescription: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(30.dp)
            )
        }
        if (label != null) {
            Text(
                text = label,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
