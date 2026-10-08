package com.rejwane.reelslocal.data.seed

import com.rejwane.reelslocal.data.database.entity.Activity
import com.rejwane.reelslocal.data.database.entity.Comment
import com.rejwane.reelslocal.data.database.entity.CommentLike
import com.rejwane.reelslocal.data.database.entity.Follow
import com.rejwane.reelslocal.data.database.entity.SavedVideo
import com.rejwane.reelslocal.data.database.entity.User
import com.rejwane.reelslocal.data.database.entity.Video
import com.rejwane.reelslocal.data.database.entity.VideoLike
import com.rejwane.reelslocal.data.model.ActivityType

/**
 * Deterministic first-launch content. All *content* (names, captions, stats, offsets) is
 * fixed — no randomness — so every installation feels identical. Timestamps are anchored to
 * seeding time with fixed offsets so the Inbox shows realistic "2m / 1h / Yesterday" ages.
 *
 * Seeded counters already include the seeded relationship rows; local interactions adjust
 * them from there (one consistent "stored base + local delta" model).
 *
 * Video files are referenced as `asset:videos/video_xxx.mp4`. The binaries are NOT bundled
 * (see README — drop your own files into app/src/main/assets/videos/). Missing files render a
 * friendly "Video unavailable" placeholder instead of crashing.
 */
object SeedData {

    const val SYSTEM_USER_ID = 1L
    const val REJWANE_ID = 2L

    private const val MINUTE = 60_000L
    private const val HOUR = 3_600_000L
    private const val DAY = 86_400_000L

    class Bundle(
        val users: List<User>,
        val videos: List<Video>,
        val comments: List<Comment>,
        val follows: List<Follow>,
        val videoLikes: List<VideoLike>,
        val savedVideos: List<SavedVideo>,
        val commentLikes: List<CommentLike>,
        val activities: List<Activity>
    )

    fun build(seedTime: Long = System.currentTimeMillis()): Bundle {
        val users = buildUsers(seedTime)
        val videos = buildVideos(seedTime)
        return Bundle(
            users = users,
            videos = videos,
            comments = buildComments(videos),
            follows = buildFollows(seedTime),
            videoLikes = buildVideoLikes(seedTime),
            savedVideos = buildSavedVideos(seedTime),
            commentLikes = buildCommentLikes(seedTime),
            activities = buildActivities(seedTime)
        )
    }

    private fun buildUsers(seedTime: Long): List<User> = listOf(
        User(
            id = SYSTEM_USER_ID, username = "system", displayName = "System",
            bio = "Orphaned local content lives here.", isSystem = true,
            createdAt = seedTime - 40 * DAY
        ),
        User(
            id = REJWANE_ID, username = "rejwane", displayName = "Rejwane",
            bio = "Welcome to my local video world.",
            followersCount = 845, followingCount = 4, likesCount = 3_200,
            createdAt = seedTime - 30 * DAY
        ),
        User(
            id = 3, username = "abonti", displayName = "অবন্তি",
            bio = "Photography | Travel | Coffee", avatarPath = "asset:images/avatar_abonti.jpg",
            followersCount = 12_400, followingCount = 3, likesCount = 45_800, isVerified = true,
            createdAt = seedTime - 29 * DAY
        ),
        User(
            id = 4, username = "sujon01", displayName = "সুজন",
            bio = "Bike lover 🏍️ | Dhaka", avatarPath = "asset:images/avatar_sujon.jpg",
            followersCount = 8_900, followingCount = 3, likesCount = 21_200,
            createdAt = seedTime - 28 * DAY
        ),
        User(
            id = 5, username = "nusrat_akter", displayName = "নুসরাত",
            bio = "Food & lifestyle 🍛", avatarPath = "asset:images/avatar_nusrat.jpg",
            followersCount = 15_200, followingCount = 5, likesCount = 60_300, isVerified = true,
            createdAt = seedTime - 27 * DAY
        ),
        User(
            id = 6, username = "rafi_22", displayName = "রাফি",
            bio = "Football ⚽ and fitness", avatarPath = "asset:images/avatar_rafi.jpg",
            followersCount = 5_600, followingCount = 2, likesCount = 12_800,
            createdAt = seedTime - 26 * DAY
        ),
        User(
            id = 7, username = "tanvir_editz", displayName = "তানভীর",
            bio = "Video editor | VFX 🎬", avatarPath = "asset:images/avatar_tanvir.jpg",
            followersCount = 20_100, followingCount = 3, likesCount = 88_400, isVerified = true,
            createdAt = seedTime - 25 * DAY
        ),
        User(
            id = 8, username = "mehedi", displayName = "মেহেদী",
            bio = "Nature photography 🌿", avatarPath = "asset:images/avatar_mehedi.jpg",
            followersCount = 4_300, followingCount = 2, likesCount = 9_800,
            createdAt = seedTime - 24 * DAY
        ),
        User(
            id = 9, username = "sadia", displayName = "সাদিয়া",
            bio = "Dance & music 💃", avatarPath = "asset:images/avatar_sadia.jpg",
            followersCount = 31_200, followingCount = 3, likesCount = 120_500, isVerified = true,
            createdAt = seedTime - 23 * DAY
        ),
        User(
            id = 10, username = "arif_hasan", displayName = "আরিফ",
            bio = "Cooking made easy 🍳", avatarPath = "asset:images/avatar_arif.jpg",
            followersCount = 7_700, followingCount = 2, likesCount = 18_900,
            createdAt = seedTime - 22 * DAY
        ),
        User(
            id = 11, username = "mim_akter", displayName = "মিম",
            bio = "Student | Bookworm 📚", avatarPath = "asset:images/avatar_mim.jpg",
            followersCount = 2_900, followingCount = 2, likesCount = 6_400,
            createdAt = seedTime - 21 * DAY
        ),
        User(
            id = 12, username = "sumaiya", displayName = "সুমাইয়া",
            bio = "Travel diaries ✈️", avatarPath = "asset:images/avatar_sumaiya.jpg",
            followersCount = 9_800, followingCount = 2, likesCount = 33_100,
            createdAt = seedTime - 20 * DAY
        )
    )

    private data class VideoSeed(
        val id: Long,
        val ownerId: Long,
        val title: String,
        val caption: String,
        val hashtags: String,
        val views: Long,
        val likes: Long,
        val comments: Long,
        val shares: Long,
        val durationMs: Long,
        val music: String
    )

    private val videoSeeds = listOf(
        VideoSeed(1, 3, "সকালের ঢাকা", "ভোরের আলোয় শহরটা অন্যরকম লাগে ✨", "travel,bangladesh,morning", 23_100, 1_840, 96, 210, 22_000, "Original Sound - অবন্তি"),
        VideoSeed(2, 4, "বাইক রাইড", "সপ্তাহান্তির রাইড, গন্তব্য অজানা 🏍️", "bike,ride,dhaka", 18_400, 1_420, 71, 143, 31_000, "Engine Beats - সুজন"),
        VideoSeed(3, 5, "বিরিয়ানি রেসিপি", "ঘরেই রেস্টুরেন্ট স্টাইল বিরিয়ানি 🍛", "food,recipe,biryani", 45_800, 3_910, 204, 486, 44_000, "Original Sound - নুসরাত"),
        VideoSeed(4, 9, "Dance Cover", "নতুন ডান্স কভার, কেমন হলো বলো 💃", "dance,music,cover", 102_300, 9_240, 412, 903, 27_000, "Trending Beat 2026"),
        VideoSeed(5, 7, "Sunset Edit", "কালার গ্রেডিং প্র্যাকটিস 🎬", "edit,vfx,sunset", 67_500, 5_380, 187, 402, 18_000, "Cinematic Waves - তানভীর"),
        VideoSeed(6, 8, "Nature Walk", "সবুজে ঘেরা পথ 🌿", "nature,photography,green", 12_700, 980, 54, 88, 35_000, "Forest Ambience"),
        VideoSeed(7, 6, "Football Skills", "ট্রেইনিং সেশন ⚽", "football,fitness,skills", 34_200, 2_610, 122, 265, 24_000, "Stadium Anthem"),
        VideoSeed(8, 10, "Street Food", "পুরান ঢাকার কাচ্চি 🍽️", "food,streetfood,kacchi", 56_900, 4_470, 233, 517, 40_000, "Original Sound - আরিফ"),
        VideoSeed(9, 11, "Book Haul", "এই মাসে যা পড়লাম 📚", "books,study,reading", 8_400, 640, 38, 41, 29_000, "Lo-fi Study - মিম"),
        VideoSeed(10, 12, "Cox's Bazar", "সমুদ্রের কাছে প্রথম সকাল 🌊", "travel,coxsbazar,sea", 78_100, 6_120, 298, 640, 33_000, "Ocean Drive - সুমাইয়া"),
        VideoSeed(11, 3, "কফি ডায়েরি", "বিকেলের কফি আর জানালার বাইরে বৃষ্টি ☕", "coffee,lifestyle,rain", 15_600, 1_230, 67, 95, 20_000, "Cafe Jazz"),
        VideoSeed(12, 2, "My First Reel", "Welcome to my local video world 👋", "first,hello,reelslocal", 1_200, 84, 12, 6, 15_000, "Original Sound - Rejwane"),
        VideoSeed(13, 4, "Night Ride", "রাতের ঢাকা, খালি রাস্তা 🌃", "bike,night,dhaka", 21_300, 1_690, 83, 154, 26_000, "Night Cruiser"),
        VideoSeed(14, 5, "Breakfast Ideas", "সকালের নাস্তা, ৩ আইডিয়া 🥣", "food,recipe,breakfast", 29_800, 2_240, 111, 201, 38_000, "Original Sound - নুসরাত"),
        VideoSeed(15, 9, "গানের কভার", "একটু গাইলাম, শোনো 🎤", "music,cover,song", 88_200, 7_650, 356, 724, 42_000, "Acoustic Session"),
        VideoSeed(16, 7, "VFX Breakdown", "বিহাইন্ড দ্য সিন 🎥", "vfx,edit,breakdown", 51_400, 4_020, 176, 389, 30_000, "Cinematic Waves - তানভীর"),
        VideoSeed(17, 8, "বৃষ্টির দিন", "মেঘলা আকাশ আর বৃষ্টির শব্দ 🌧️", "nature,rain,monsoon", 11_200, 870, 45, 62, 25_000, "Rain Drops"),
        VideoSeed(18, 6, "Workout Routine", "ফুল বডি ওয়ার্কআউট 💪", "fitness,gym,workout", 26_700, 2_030, 98, 187, 36_000, "Pump It Up"),
        VideoSeed(19, 10, "চা বানানোর আর্ট", "দোকানি ভাইয়ের হাতের চা 🍵", "tea,food,streetfood", 41_500, 3_260, 165, 298, 21_000, "Original Sound - আরিফ"),
        VideoSeed(20, 11, "Study Tips", "পরীক্ষার আগে যেভাবে পড়ি 📝", "study,tips,exam", 9_900, 720, 41, 53, 32_000, "Lo-fi Study - মিম"),
        VideoSeed(21, 12, "পাহাড়ের পথে", "বান্দরবান ট্রেক, মেঘ ছুঁয়ে দেখা 🏔️", "travel,hills,bandarban", 63_400, 4_980, 241, 512, 45_000, "Mountain Air - সুমাইয়া"),
        VideoSeed(22, 3, "Old Dhaka", "পুরান ঢাকার গলি, সময়ের গন্ধ 🏘️", "travel,olddhaka,heritage", 32_900, 2_540, 129, 276, 28_000, "City of Memories"),
        VideoSeed(23, 2, "Setup Tour", "আমার ছোট্ট সেটআপ 🖥️", "setup,tech,desk", 2_400, 156, 18, 9, 23_000, "Original Sound - Rejwane"),
        VideoSeed(24, 9, "রিহার্সেল", "শো'র আগে শেষ মুহূর্ত 💃", "dance,rehearsal,show", 74_600, 5_910, 287, 611, 19_000, "Trending Beat 2026")
    )

    private fun buildVideos(seedTime: Long): List<Video> = videoSeeds.map { seed ->
        Video(
            id = seed.id,
            ownerId = seed.ownerId,
            videoPath = "asset:videos/video_%03d.mp4".format(seed.id),
            thumbnailPath = "asset:images/thumb_%03d.jpg".format(seed.id),
            title = seed.title,
            caption = seed.caption,
            hashtags = seed.hashtags,
            musicTitle = seed.music,
            views = seed.views,
            likesCount = seed.likes,
            commentsCount = seed.comments,
            sharesCount = seed.shares,
            durationMs = seed.durationMs,
            isPublished = true,
            sortOrder = seed.id.toInt(),
            // Newest first: video 24 is the most recent.
            createdAt = seedTime - (24 - seed.id) * 7 * HOUR
        )
    }

    private val commentTexts = listOf(
        "অনেক সুন্দর ❤️",            // 0
        "বিউটিফুল 😍",                // 1
        "Wow!",                        // 2
        "একদম অসাধারণ",              // 3
        "Nice video",                  // 4
        "❤️❤️❤️",                      // 5
        "দারুণ হয়েছে 🔥",             // 6
        "Nice shot!",                  // 7
        "ভিডিওটা অনেক ভালো হয়েছে",   // 8
        "এক কথায় দারুণ 🔥",           // 9
        "কী সুন্দর!",                  // 10
        "Love this!",                  // 11
        "দারুণ লাগছে",                 // 12
        "Nice one ❤️",                 // 13
        "পরের ভিডিওর অপেক্ষায় 👀",   // 14
        "Best edit ever 🎬",           // 15
        "ক্যামেরা ওয়ার্ক দারুণ",      // 16
        "এটা কোথায় শুট করেছ?",        // 17
        "সেরা! 🔥",                    // 18
        "আরও এমন ভিডিও চাই",         // 19
        "Too good ❤️",                 // 20
        "Goosebumps ✨",               // 21
        "এক কথায় অসাধারণ",           // 22
        "Haha nice 😂"                 // 23
    )

    // videoId, authorId, textIndex — fixed table, varied authors and texts per video.
    private val commentSeeds = listOf(
        Triple(1L, 4L, 0), Triple(1L, 5L, 1), Triple(1L, 9L, 6), Triple(1L, 2L, 8),
        Triple(2L, 3L, 7), Triple(2L, 6L, 9), Triple(2L, 2L, 4),
        Triple(3L, 3L, 0), Triple(3L, 7L, 3), Triple(3L, 11L, 10), Triple(3L, 9L, 5), Triple(3L, 2L, 8),
        Triple(4L, 3L, 1), Triple(4L, 5L, 21), Triple(4L, 12L, 18), Triple(4L, 7L, 15),
        Triple(5L, 4L, 15), Triple(5L, 9L, 3), Triple(5L, 2L, 20),
        Triple(6L, 3L, 10), Triple(6L, 12L, 12),
        Triple(7L, 4L, 6), Triple(7L, 10L, 4), Triple(7L, 2L, 13),
        Triple(8L, 5L, 0), Triple(8L, 3L, 9), Triple(8L, 11L, 1), Triple(8L, 6L, 19),
        Triple(9L, 5L, 11), Triple(9L, 12L, 14),
        Triple(10L, 3L, 21), Triple(10L, 9L, 0), Triple(10L, 7L, 16), Triple(10L, 2L, 12),
        Triple(11L, 4L, 10), Triple(11L, 5L, 20), Triple(11L, 2L, 1),
        Triple(12L, 3L, 8), Triple(12L, 5L, 6), Triple(12L, 9L, 2),
        Triple(13L, 6L, 18), Triple(13L, 3L, 4),
        Triple(14L, 10L, 9), Triple(14L, 11L, 0), Triple(14L, 2L, 10),
        Triple(15L, 3L, 22), Triple(15L, 7L, 21), Triple(15L, 5L, 5), Triple(15L, 12L, 11),
        Triple(16L, 4L, 15), Triple(16L, 9L, 16), Triple(16L, 2L, 6),
        Triple(17L, 12L, 12), Triple(17L, 3L, 0),
        Triple(18L, 6L, 3), Triple(18L, 4L, 13),
        Triple(19L, 5L, 10), Triple(19L, 8L, 19), Triple(19L, 2L, 9),
        Triple(20L, 9L, 14), Triple(20L, 11L, 4),
        Triple(21L, 3L, 18), Triple(21L, 5L, 21), Triple(21L, 10L, 7), Triple(21L, 2L, 11),
        Triple(22L, 7L, 16), Triple(22L, 12L, 0),
        Triple(23L, 3L, 4), Triple(23L, 4L, 23),
        Triple(24L, 5L, 22), Triple(24L, 3L, 6), Triple(24L, 11L, 2), Triple(24L, 2L, 20)
    )

    private fun buildComments(videos: List<Video>): List<Comment> =
        commentSeeds.mapIndexed { index, (videoId, authorId, textIndex) ->
            val video = videos.first { it.id == videoId }
            val sameVideoIndex = commentSeeds.take(index).count { it.first == videoId }
            Comment(
                id = (index + 1).toLong(),
                videoId = videoId,
                authorId = authorId,
                text = commentTexts[textIndex],
                likeCount = ((textIndex * 7 + authorId * 3 + videoId) % 60) + sameVideoIndex * 5,
                isPinned = videoId == 3L && authorId == 3L,
                isHidden = false,
                sortOrder = sameVideoIndex,
                createdAt = video.createdAt + (sameVideoIndex + 1) * 47 * MINUTE
            )
        }

    private val followPairs = listOf(
        2L to 3L, 2L to 4L, 2L to 5L, 2L to 9L,
        3L to 4L, 3L to 5L, 3L to 7L,
        4L to 5L, 4L to 6L,
        5L to 3L, 5L to 9L, 5L to 12L,
        6L to 4L, 6L to 9L,
        7L to 3L, 7L to 5L, 7L to 9L,
        8L to 7L, 8L to 3L,
        9L to 3L, 9L to 5L, 9L to 7L,
        10L to 5L, 10L to 9L,
        11L to 9L, 11L to 3L,
        12L to 5L, 12L to 9L
    )

    private fun buildFollows(seedTime: Long): List<Follow> =
        followPairs.mapIndexed { index, (follower, following) ->
            Follow(
                followerId = follower,
                followingId = following,
                createdAt = seedTime - (followPairs.size - index) * 6 * HOUR
            )
        }

    private val likePairs = listOf(
        2L to 1L, 2L to 3L, 2L to 5L, 2L to 8L, 2L to 12L, 2L to 16L,
        3L to 2L, 3L to 7L,
        5L to 4L, 5L to 9L,
        9L to 15L,
        7L to 24L
    )

    private fun buildVideoLikes(seedTime: Long): List<VideoLike> =
        likePairs.mapIndexed { index, (user, video) ->
            VideoLike(userId = user, videoId = video, createdAt = seedTime - (index + 1) * 5 * HOUR)
        }

    private val savedVideoIds = listOf(2L, 5L, 9L, 14L)

    private fun buildSavedVideos(seedTime: Long): List<SavedVideo> =
        savedVideoIds.mapIndexed { index, videoId ->
            SavedVideo(userId = REJWANE_ID, videoId = videoId, createdAt = seedTime - (index + 1) * 9 * HOUR)
        }

    private val commentLikePairs = listOf(
        2L to 1L, 2L to 5L, 2L to 9L, 3L to 2L, 5L to 4L
    )

    private fun buildCommentLikes(seedTime: Long): List<CommentLike> =
        commentLikePairs.map { (user, comment) ->
            CommentLike(userId = user, commentId = comment, createdAt = seedTime - 3 * HOUR)
        }

    private fun buildActivities(seedTime: Long): List<Activity> = listOf(
        Activity(
            id = 1, userId = REJWANE_ID, actorId = REJWANE_ID,
            type = ActivityType.FOLLOW_USER.name, targetUserId = 3,
            message = "তুমি অবন্তিকে Follow করেছ",
            isRead = true, createdAt = seedTime - 2 * DAY
        ),
        Activity(
            id = 2, userId = REJWANE_ID, actorId = REJWANE_ID,
            type = ActivityType.FOLLOW_USER.name, targetUserId = 4,
            message = "তুমি সুজনকে Follow করেছ",
            isRead = true, createdAt = seedTime - 2 * DAY + 5 * MINUTE
        ),
        Activity(
            id = 3, userId = REJWANE_ID, actorId = REJWANE_ID,
            type = ActivityType.LIKE_VIDEO.name, targetVideoId = 1,
            message = "তুমি অবন্তির ভিডিওতে লাইক দিয়েছ",
            isRead = false, createdAt = seedTime - 1 * DAY
        ),
        Activity(
            id = 4, userId = REJWANE_ID, actorId = REJWANE_ID,
            type = ActivityType.FOLLOW_USER.name, targetUserId = 5,
            message = "তুমি নুসরাতকে Follow করেছ",
            isRead = true, createdAt = seedTime - 1 * DAY + 2 * HOUR
        ),
        Activity(
            id = 5, userId = REJWANE_ID, actorId = REJWANE_ID,
            type = ActivityType.LIKE_VIDEO.name, targetVideoId = 3,
            message = "তুমি নুসরাতের ভিডিওতে লাইক দিয়েছ",
            isRead = true, createdAt = seedTime - 6 * HOUR
        ),
        Activity(
            id = 6, userId = REJWANE_ID, actorId = REJWANE_ID,
            type = ActivityType.COMMENT_VIDEO.name, targetVideoId = 2, targetCommentId = 7,
            message = "তুমি সুজনের ভিডিওতে কমেন্ট করেছ",
            isRead = false, createdAt = seedTime - 3 * HOUR
        ),
        Activity(
            id = 7, userId = REJWANE_ID, actorId = REJWANE_ID,
            type = ActivityType.FOLLOW_USER.name, targetUserId = 9,
            message = "তুমি সাদিয়াকে Follow করেছ",
            isRead = true, createdAt = seedTime - 2 * HOUR
        ),
        Activity(
            id = 8, userId = REJWANE_ID, actorId = REJWANE_ID,
            type = ActivityType.LIKE_VIDEO.name, targetVideoId = 5,
            message = "তুমি তানভীরের ভিডিওতে লাইক দিয়েছ",
            isRead = false, createdAt = seedTime - 30 * MINUTE
        ),
        Activity(
            id = 9, userId = REJWANE_ID, actorId = REJWANE_ID,
            type = ActivityType.SAVE_VIDEO.name, targetVideoId = 9,
            message = "তুমি মিমের ভিডিও সেভ করেছ",
            isRead = true, createdAt = seedTime - 2 * MINUTE
        )
    )
}
