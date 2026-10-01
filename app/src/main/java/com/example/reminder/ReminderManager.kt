package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.Exam
import com.example.data.model.TimetableClass
import java.util.Calendar

class ReminderManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    init {
        ReminderBroadcastReceiver.createNotificationChannel(context)
    }

    fun scheduleAlert(
        triggerTimeMillis: Long,
        title: String,
        time: String,
        venue: String = "",
        message: String = "",
        notificationId: Int,
        isMorningBriefing: Boolean = false,
        isExam: Boolean = false,
        isSessionStarted: Boolean = false
    ) {
        if (triggerTimeMillis <= System.currentTimeMillis()) {
            return
        }

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra(ReminderBroadcastReceiver.EXTRA_TITLE, title)
            putExtra(ReminderBroadcastReceiver.EXTRA_TIME, time)
            putExtra(ReminderBroadcastReceiver.EXTRA_VENUE, venue)
            putExtra(ReminderBroadcastReceiver.EXTRA_MESSAGE, message.ifBlank { "You have $title at $time" })
            putExtra(ReminderBroadcastReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(ReminderBroadcastReceiver.EXTRA_IS_MORNING, isMorningBriefing)
            putExtra(ReminderBroadcastReceiver.EXTRA_IS_EXAM, isExam)
            putExtra(ReminderBroadcastReceiver.EXTRA_IS_SESSION_STARTED, isSessionStarted)
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
     * Schedules reminders for today's classes:
     * 1. Early morning briefing (custom seconds/minutes/hours prior)
     * 2. Advance pre-session alert (custom seconds/minutes/hours prior)
     * 3. Session starting / started alert (at EXACT session start time)
     */
    fun scheduleTodayClassReminders(
        todayClasses: List<TimetableClass>,
        morningOffsetSeconds: Int = 7200,
        sessionOffsetSeconds: Int = 1800,
        notifyOnSessionStart: Boolean = true,
        calendar: Calendar = Calendar.getInstance()
    ) {
        if (todayClasses.isEmpty()) return
        val sorted = todayClasses.sortedBy { it.startMinutes() }

        // 1. Morning wake-up reminder for the earliest student class (Student Mode only, not Lecturers)
        val firstStudentClass = sorted.firstOrNull { it.role == "STUDENT" && it.isMorningBriefingEnabled }
        if (firstStudentClass != null) {
            val morningCal = calendar.clone() as Calendar
            val startMins = firstStudentClass.startMinutes()
            morningCal.set(Calendar.HOUR_OF_DAY, startMins / 60)
            morningCal.set(Calendar.MINUTE, startMins % 60)
            morningCal.set(Calendar.SECOND, 0)
            morningCal.set(Calendar.MILLISECOND, 0)
            // Subtract custom seconds/minutes/hours
            morningCal.add(Calendar.SECOND, -morningOffsetSeconds)

            val alertTime = morningCal.timeInMillis
            val venue = firstStudentClass.location.ifBlank { "Campus" }
            val title = "${firstStudentClass.courseCode} (${firstStudentClass.courseName})"
            val msg = "Wake up and get ready for your morning class! You have $title at ${firstStudentClass.startTime} in $venue."
            val notifId = (firstStudentClass.id * 100 + 1).toInt()

            scheduleAlert(
                triggerTimeMillis = alertTime,
                title = title,
                time = firstStudentClass.startTime,
                venue = venue,
                message = msg,
                notificationId = notifId,
                isMorningBriefing = true
            )
        }

        // 2. Pre-session & Session Started reminder for each class
        for (session in sorted) {
            val venue = session.location.ifBlank { "Venue TBD" }
            val title = "${session.courseCode}: ${session.courseName}"

            // 2A. Pre-session advance alert (custom seconds/minutes/hours)
            if (session.isPreSessionReminderEnabled) {
                val offsetSec = session.totalReminderSeconds(sessionOffsetSeconds)

                val sessionCal = calendar.clone() as Calendar
                val startMins = session.startMinutes()
                sessionCal.set(Calendar.HOUR_OF_DAY, startMins / 60)
                sessionCal.set(Calendar.MINUTE, startMins % 60)
                sessionCal.set(Calendar.SECOND, 0)
                sessionCal.set(Calendar.MILLISECOND, 0)
                sessionCal.add(Calendar.SECOND, -offsetSec)

                val alertTime = sessionCal.timeInMillis
                val msg = "Prepare! You have $title at ${session.startTime} in $venue."
                val notifId = (session.id * 100 + 2).toInt()

                scheduleAlert(
                    triggerTimeMillis = alertTime,
                    title = title,
                    time = session.startTime,
                    venue = venue,
                    message = msg,
                    notificationId = notifId,
                    isMorningBriefing = false
                )
            }

            // 2B. NEW FEATURE: Session Starting / Started Alert (At exact start time)
            if (notifyOnSessionStart && session.notifyOnSessionStart) {
                val startCal = calendar.clone() as Calendar
                val startMins = session.startMinutes()
                startCal.set(Calendar.HOUR_OF_DAY, startMins / 60)
                startCal.set(Calendar.MINUTE, startMins % 60)
                startCal.set(Calendar.SECOND, 0)
                startCal.set(Calendar.MILLISECOND, 0)

                val alertTime = startCal.timeInMillis
                val msg = "Session Starting NOW at $venue! Class has begun."
                val notifId = (session.id * 100 + 3).toInt()

                scheduleAlert(
                    triggerTimeMillis = alertTime,
                    title = title,
                    time = session.startTime,
                    venue = venue,
                    message = msg,
                    notificationId = notifId,
                    isMorningBriefing = false,
                    isSessionStarted = true
                )
            }
        }
    }

    /**
     * Schedules exam reminders
     */
    fun scheduleExamReminder(exam: Exam, calendar: Calendar = Calendar.getInstance()) {
        val parts = exam.examDate.split("-")
        if (parts.size < 3) return
        val y = parts[0].toIntOrNull() ?: return
        val m = (parts[1].toIntOrNull() ?: 1) - 1
        val d = parts[2].toIntOrNull() ?: return

        val examCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m)
            set(Calendar.DAY_OF_MONTH, d)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        examCal.add(Calendar.DAY_OF_YEAR, -1)
        val alertTime = examCal.timeInMillis

        val title = "EXAM: ${exam.unitCode} - ${exam.unitTitle}"
        val venue = exam.venue.ifBlank { "Main Exam Hall" }
        val msg = "Priority 1 Alert: Upcoming exam tomorrow at ${if (exam.examTime.isNotBlank()) exam.examTime else "scheduled time"}. Venue: $venue. Study now!"

        scheduleAlert(
            triggerTimeMillis = alertTime,
            title = title,
            time = if (exam.examTime.isNotBlank()) exam.examTime else "Tomorrow",
            venue = venue,
            message = msg,
            notificationId = (exam.id * 1000 + 7).toInt(),
            isExam = true
        )
    }

    fun triggerTestFullScreenAlarm(isMorning: Boolean, isSessionStarted: Boolean = false) {
        val intent = Intent(context, FullScreenReminderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(FullScreenReminderActivity.EXTRA_TITLE, if (isMorning) "Morning Class Briefing" else "Upcoming Class Session")
            putExtra(FullScreenReminderActivity.EXTRA_TIME, if (isMorning) "08:30" else "11:00")
            putExtra(FullScreenReminderActivity.EXTRA_VENUE, "Lecture Hall 3B")
            putExtra(FullScreenReminderActivity.EXTRA_IS_MORNING, isMorning)
            putExtra(FullScreenReminderActivity.EXTRA_IS_EXAM, false)
            putExtra(FullScreenReminderActivity.EXTRA_IS_SESSION_STARTED, isSessionStarted)
        }
        context.startActivity(intent)
    }
}
