package com.rejwane.reelslocal

import android.app.Application
import com.rejwane.reelslocal.di.AppInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ReelsLocalApplication : Application() {

    @Inject
    lateinit var initializer: AppInitializer

    override fun onCreate() {
        super.onCreate()
        initializer.initialize()
    }
}
