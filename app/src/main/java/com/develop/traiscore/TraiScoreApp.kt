package com.develop.traiscore

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TraiScoreApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}