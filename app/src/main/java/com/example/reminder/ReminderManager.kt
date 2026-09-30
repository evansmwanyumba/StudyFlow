package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.DayTask
import com.example.data.model.TimetableClass
import java.util.Calendar

class ReminderManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    init {
        ReminderBroadcastReceiver.createNotificationChannel(context)
    }

    /**
     * Schedules a notification for a specific epoch timestamp.
     */
    fun scheduleAlert(
        triggerTimeMillis: Long,
        title: String,
        message: String,
        notificationId: Int,
        isMorningBriefing: Boolean = false
    ) {
        if (triggerTimeMillis <= System.currentTimeMillis()) {
            return
        }

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra(ReminderBroadcastReceiver.EXTRA_TITLE, title)
            putExtra(ReminderBroadcastReceiver.EXTRA_MESSAGE, message)
            putExtra(ReminderBroadcastReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(ReminderBroadcastReceiver.EXTRA_IS_MORNING, isMorningBriefing)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager?.canScheduleExactAlarms() == true) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager?.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager?.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        } catch (_: Exception) {
            alarmManager?.set(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        }
    }

    /**
     * Calculates and schedules reminders for today's classes:
     * - 2 hours before the first morning class
     * - 30 minutes before each class
     */
    fun scheduleTodayClassReminders(todayClasses: List<TimetableClass>, calendar: Calendar = Calendar.getInstance()) {
        if (todayClasses.isEmpty()) return

        val sorted = todayClasses.sortedBy { it.startMinutes() }

        // 1. Morning 2-hour early alert for the earliest class
        val firstClass = sorted.firstOrNull { it.isMorningBriefingEnabled }
        if (firstClass != null) {
            val morningCal = calendar.clone() as Calendar
            val startMins = firstClass.startMinutes()
            val hour = startMins / 60
            val min = startMins % 60

            morningCal.set(Calendar.HOUR_OF_DAY, hour)
            morningCal.set(Calendar.MINUTE, min)
            morningCal.set(Calendar.SECOND, 0)
            morningCal.set(Calendar.MILLISECOND, 0)

            // Subtract 2 hours (120 minutes)
            morningCal.add(Calendar.MINUTE, -120)

            val alertTime = morningCal.timeInMillis
            val title = "🌅 Morning Briefing: Class in 2 Hours!"
            val roomPart = if (firstClass.location.isNotBlank()) " in ${firstClass.location}" else ""
            val message = "Your first session ${firstClass.courseCode} (${firstClass.courseName}) begins at ${firstClass.startTime}$roomPart. Time to get ready!"
            val notifId = (firstClass.id * 100 + 1).toInt()

            scheduleAlert(alertTime, title, message, notifId, isMorningBriefing = true)
        }

        // 2. 30-minute pre-session reminder for each class
        for (session in sorted) {
            if (!session.isPreSessionReminderEnabled) continue

            val sessionCal = calendar.clone() as Calendar
            val startMins = session.startMinutes()
            val hour = startMins / 60
            val min = startMins % 60

            sessionCal.set(Calendar.HOUR_OF_DAY, hour)
            sessionCal.set(Calendar.MINUTE, min)
            sessionCal.set(Calendar.SECOND, 0)
            sessionCal.set(Calendar.MILLISECOND, 0)

            // Subtract 30 minutes
            sessionCal.add(Calendar.MINUTE, -30)

            val alertTime = sessionCal.timeInMillis
            val title = "🔔 Class Starting in 30 Minutes"
            val roomPart = if (session.location.isNotBlank()) " • Room: ${session.location}" else ""
            val message = "${session.courseCode} - ${session.courseName} starts at ${session.startTime}$roomPart."
            val notifId = (session.id * 100 + 2).toInt()

            scheduleAlert(alertTime, title, message, notifId, isMorningBriefing = false)
        }
    }

    /**
     * Sends an immediate test notification for UI verification.
     */
    fun sendInstantTestNotification(isMorningBriefing: Boolean) {
        if (isMorningBriefing) {
            ReminderBroadcastReceiver.showNotification(
                context,
                "🌅 Morning Class Briefing (2h Alert)",
                "First class CS101: Data Structures begins at 09:00 in Hall B. You have 2 hours to prepare!",
                9991,
                isMorningBriefing = true
            )
        } else {
            ReminderBroadcastReceiver.showNotification(
                context,
                "🔔 30-Minute Session Alert",
                "MATH 240: Linear Algebra starts at 10:30 in Room 204. Grab your notebook and calculator!",
                9992,
                isMorningBriefing = false
            )
        }
    }
}
