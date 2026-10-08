package com.rejwane.reelslocal.utils

import java.util.Locale
import java.util.concurrent.TimeUnit

/** Compact, consistent count formatting: 999, 1.2K, 125K, 1.2M. */
fun formatCount(count: Long): String = when {
    count < 0 -> "0"
    count < 1_000 -> count.toString()
    count < 100_000 -> trimDecimal(count / 1_000.0) + "K"
    count < 1_000_000 -> (count / 1_000).toString() + "K"
    else -> trimDecimal(count / 1_000_000.0) + "M"
}

private fun trimDecimal(value: Double): String {
    val rounded = Math.round(value * 10) / 10.0
    return if (rounded >= 100) {
        rounded.toInt().toString()
    } else if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", rounded)
    }
}

/** Milliseconds to m:ss / h:mm:ss. */
fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0) return "0:00"
    val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}

/** Relative display time: just now, 2m, 1h, Yesterday, 3d, then date-like weeks. */
fun formatRelativeTime(timestamp: Long, now: Long = System.currentTimeMillis()): String {
    val diff = (now - timestamp).coerceAtLeast(0)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        days == 1L -> "Yesterday"
        days < 7 -> "${days}d"
        days < 28 -> "${days / 7}w"
        else -> "${days / 30}mo"
    }
}
