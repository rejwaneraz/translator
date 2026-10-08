package com.rejwane.reelslocal.ui.player

import android.graphics.Color
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.ExoPlayer
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

/**
 * Renders the shared [ExoPlayer] full-bleed. Only the currently active feed page composes this,
 * so exactly one [PlayerView] ever holds the player's video surface — no surface hand-off races.
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerSurface(
    player: ExoPlayer,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    AndroidView(
        modifier = modifier,
        factory = {
            PlayerView(context).apply {
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setShutterBackgroundColor(Color.BLACK)
            }
        },
        update = { playerView ->
            playerView.player = player
        }
    )
}
