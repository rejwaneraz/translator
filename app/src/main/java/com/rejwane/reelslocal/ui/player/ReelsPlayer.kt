package com.rejwane.reelslocal.ui.player

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.ExoPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi

/**
 * A single shared [ExoPlayer] reused across the whole vertical feed, so only one decoder
 * is ever alive. It loops, respects audio focus, and pauses with the host lifecycle.
 * Released automatically when the caller leaves composition.
 */
@OptIn(UnstableApi::class)
@Composable
fun rememberReelsPlayer(): ExoPlayer {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context)
            .setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply { repeatMode = Player.REPEAT_MODE_ONE }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> player.pause()
                Lifecycle.Event.ON_STOP -> player.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.release()
        }
    }
    return player
}
