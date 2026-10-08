package com.rejwane.reelslocal.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.rejwane.reelslocal.data.prefs.DataStoreProvider
import com.rejwane.reelslocal.data.prefs.DataStoreSettingsRepository
import com.rejwane.reelslocal.data.prefs.SettingsRepository
import com.rejwane.reelslocal.data.repository.ActivityRepository
import com.rejwane.reelslocal.data.repository.CommentRepository
import com.rejwane.reelslocal.data.repository.LocalActivityRepository
import com.rejwane.reelslocal.data.repository.LocalCommentRepository
import com.rejwane.reelslocal.data.repository.LocalSocialRepository
import com.rejwane.reelslocal.data.repository.LocalUserRepository
import com.rejwane.reelslocal.data.repository.LocalVideoRepository
import com.rejwane.reelslocal.data.repository.SocialRepository
import com.rejwane.reelslocal.data.repository.UserRepository
import com.rejwane.reelslocal.data.repository.VideoRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        DataStoreProvider.create(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: LocalUserRepository): UserRepository

    @Binds
    @Singleton
    abstract fun bindVideoRepository(impl: LocalVideoRepository): VideoRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(impl: LocalCommentRepository): CommentRepository

    @Binds
    @Singleton
    abstract fun bindSocialRepository(impl: LocalSocialRepository): SocialRepository

    @Binds
    @Singleton
    abstract fun bindActivityRepository(impl: LocalActivityRepository): ActivityRepository
}
