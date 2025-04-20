package com.magic.habbittracker.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import androidx.work.ExistingWorkPolicy
import com.magic.habbittracker.MainActivity
import com.magic.habbittracker.R
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "habit_reminder_channel"
        private const val NOTIFICATION_ID = 1
        private const val WORK_TAG = "habit_reminder_work"
        
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders for your habits"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleReminder(timeString: String, enabled: Boolean) {
        // Cancel any existing reminders
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWork() // Cancel all existing work
        
        if (!enabled) return
        
        try {
            // Parse the time string
            val time = LocalTime.parse(timeString, TIME_FORMATTER)
            val now = LocalDateTime.now()
            val scheduledTime = LocalDateTime.of(now.toLocalDate(), time)
            
            // If time already passed today, schedule for tomorrow
            var initialDelay = Duration.between(now, scheduledTime)
            if (initialDelay.isNegative) {
                initialDelay = Duration.between(now, scheduledTime.plusDays(1))
            }
            
            // Create work request
            val reminderWork = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
                .build()
            
            // Schedule work
            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniqueWork(
                WORK_TAG,
                ExistingWorkPolicy.REPLACE,
                reminderWork
            )
        } catch (e: Exception) {
            // Fallback to a default time if parsing fails
            e.printStackTrace()
        }
    }

    fun showNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)  // System reminder icon
            .setContentTitle("Habit Reminder")
            .setContentText("Don't forget to complete your habits for today!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
            
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {
    
    override fun doWork(): Result {
        val notificationHelper = NotificationHelper(applicationContext)
        
        notificationHelper.showNotification()
        
        val preferencesRepo = UserPreferencesRepository(applicationContext)
        val timeString = "08:00"
        
        notificationHelper.scheduleReminder(timeString, true)
        
        return Result.success()
    }
}