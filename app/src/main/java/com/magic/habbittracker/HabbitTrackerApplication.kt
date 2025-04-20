package com.magic.habbittracker

import android.app.Application
import androidx.work.Configuration

/**
 * Custom Application class for initializing WorkManager
 */
class HabbitTrackerApplication : Application(), Configuration.Provider {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide configurations here
    }
    
    /**
     * Provides WorkManager configuration
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}