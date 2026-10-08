package com.rejwane.reelslocal.di

import android.content.Context
import androidx.room.Room
import com.rejwane.reelslocal.data.database.AppDatabase
import com.rejwane.reelslocal.data.database.dao.ActivityDao
import com.rejwane.reelslocal.data.database.dao.CommentDao
import com.rejwane.reelslocal.data.database.dao.SocialDao
import com.rejwane.reelslocal.data.database.dao.UserDao
import com.rejwane.reelslocal.data.database.dao.VideoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .build()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideVideoDao(db: AppDatabase): VideoDao = db.videoDao()

    @Provides
    fun provideCommentDao(db: AppDatabase): CommentDao = db.commentDao()

    @Provides
    fun provideSocialDao(db: AppDatabase): SocialDao = db.socialDao()

    @Provides
    fun provideActivityDao(db: AppDatabase): ActivityDao = db.activityDao()
}
