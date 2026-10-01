package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "StudyFlow Reminder"
        val time = intent.getStringExtra(EXTRA_TIME) ?: ""
        val venue = intent.getStringExtra(EXTRA_VENUE) ?: ""
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "You have a task scheduled."
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, (System.currentTimeMillis() % 100000).toInt())
        val isMorningBriefing = intent.getBooleanExtra(EXTRA_IS_MORNING, false)
        val isExam = intent.getBooleanExtra(EXTRA_IS_EXAM, false)
        val isSessionStarted = intent.getBooleanExtra(EXTRA_IS_SESSION_STARTED, false)

        // 1. Pop up full screen activity
        try {
            val fullScreenIntent = Intent(context, FullScreenReminderActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(FullScreenReminderActivity.EXTRA_TITLE, title)
                putExtra(FullScreenReminderActivity.EXTRA_TIME, time)
                putExtra(FullScreenReminderActivity.EXTRA_VENUE, venue)
                putExtra(FullScreenReminderActivity.EXTRA_IS_MORNING, isMorningBriefing)
                putExtra(FullScreenReminderActivity.EXTRA_IS_EXAM, isExam)
                putExtra(FullScreenReminderActivity.EXTRA_IS_SESSION_STARTED, isSessionStarted)
            }
            context.startActivity(fullScreenIntent)
        } catch (_: Exception) {
        }

        // 2. Also post heads-up notification with full screen intent
        showNotification(
            context = context,
            title = title,
            time = time,
            venue = venue,
            message = message,
            notificationId = notificationId,
            isMorningBriefing = isMorningBriefing,
            isExam = isExam,
            isSessionStarted = isSessionStarted
        )
    }

    companion object {
        const val CHANNEL_ID = "studyflow_schedule_channel"
        const val CHANNEL_NAME = "Class & Task Reminders"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TIME = "extra_time"
        const val EXTRA_VENUE = "extra_venue"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_IS_MORNING = "extra_is_morning"
        const val EXTRA_IS_EXAM = "extra_is_exam"
        const val EXTRA_IS_SESSION_STARTED = "extra_is_session_started"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Timetable morning briefings, sessions, and exam alerts"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 800, 200, 800, 200, 800, 400)
                    setSound(soundUri, audioAttributes)
                    setShowBadge(true)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                }
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun showNotification(
            context: Context,
            title: String,
            time: String,
            venue: String,
            message: String,
            notificationId: Int,
            isMorningBriefing: Boolean,
            isExam: Boolean = false,
            isSessionStarted: Boolean = false
        ) {
            createNotificationChannel(context)

            val fullScreenIntent = Intent(context, FullScreenReminderActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(FullScreenReminderActivity.EXTRA_TITLE, title)
                putExtra(FullScreenReminderActivity.EXTRA_TIME, time)
                putExtra(FullScreenReminderActivity.EXTRA_VENUE, venue)
                putExtra(FullScreenReminderActivity.EXTRA_IS_MORNING, isMorningBriefing)
                putExtra(FullScreenReminderActivity.EXTRA_IS_EXAM, isExam)
                putExtra(FullScreenReminderActivity.EXTRA_IS_SESSION_STARTED, isSessionStarted)
            }
            val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId + 1000,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            when {
                isExam -> builder.setSubText("Exam Alert • Priority 1")
                isSessionStarted -> builder.setSubText("Session Starting NOW!")
                isMorningBriefing -> builder.setSubText("Morning Briefing")
                else -> builder.setSubText("Session Alert")
            }

            try {
                NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            } catch (_: SecurityException) {
            }
        }
    }
}
