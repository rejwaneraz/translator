package com.rejwane.reelslocal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rejwane.reelslocal.utils.MediaPaths
import com.rejwane.reelslocal.utils.formatCount
import com.rejwane.reelslocal.utils.formatDuration

/**
 * 3-column grid tile in 9:16 with a poster thumbnail, a bottom-left view count (▶ 8.2K) and a
 * bottom-right duration. Never crashes on a missing thumbnail — the surface colour shows through.
 */
@Composable
fun VideoGridItem(
    thumbnailPath: String?,
    views: Long,
    durationMs: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = MediaPaths.imageModel(thumbnailPath),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                    )
                )
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Text(
                text = "▶ ${formatCount(views)}",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Text(
                text = formatDuration(durationMs),
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}
