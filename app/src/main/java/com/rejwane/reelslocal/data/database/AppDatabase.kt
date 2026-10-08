package com.rejwane.reelslocal.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rejwane.reelslocal.data.database.dao.ActivityDao
import com.rejwane.reelslocal.data.database.dao.CommentDao
import com.rejwane.reelslocal.data.database.dao.SocialDao
import com.rejwane.reelslocal.data.database.dao.UserDao
import com.rejwane.reelslocal.data.database.dao.VideoDao
import com.rejwane.reelslocal.data.database.entity.Activity
import com.rejwane.reelslocal.data.database.entity.Comment
import com.rejwane.reelslocal.data.database.entity.CommentLike
import com.rejwane.reelslocal.data.database.entity.Follow
import com.rejwane.reelslocal.data.database.entity.SavedVideo
import com.rejwane.reelslocal.data.database.entity.User
import com.rejwane.reelslocal.data.database.entity.Video
import com.rejwane.reelslocal.data.database.entity.VideoLike

@Database(
    entities = [
        User::class,
        Video::class,
        Comment::class,
        Follow::class,
        VideoLike::class,
        CommentLike::class,
        SavedVideo::class,
        Activity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun videoDao(): VideoDao
    abstract fun commentDao(): CommentDao
    abstract fun socialDao(): SocialDao
    abstract fun activityDao(): ActivityDao

    companion object {
        const val NAME = "reelslocal.db"
    }
}
