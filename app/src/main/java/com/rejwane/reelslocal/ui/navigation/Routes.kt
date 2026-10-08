package com.rejwane.reelslocal.ui.navigation

object Routes {
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val INBOX = "inbox"
    const val PROFILE = "profile"
    const val SEARCH = "search"

    const val PROFILE_DETAIL_ARG = "userId"
    const val PROFILE_DETAIL = "profile/{$PROFILE_DETAIL_ARG}"
    fun profileDetail(userId: Long) = "profile/$userId"

    const val VIDEO_ARG = "videoId"
    const val VIDEO = "video/{$VIDEO_ARG}"
    fun video(videoId: Long) = "video/$videoId"

    const val HASHTAG_ARG = "tag"
    const val HASHTAG = "hashtag/{$HASHTAG_ARG}"
    fun hashtag(tag: String) = "hashtag/$tag"

    const val SETTINGS = "settings"
    const val CONTENT_MANAGER = "content_manager"
    const val BACKUP = "backup"
}
