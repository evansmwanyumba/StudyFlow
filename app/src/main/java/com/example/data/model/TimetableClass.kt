package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable_classes")
data class TimetableClass(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseCode: String, // Unit Code (e.g. CS101, ENG201)
    val courseName: String, // Unit Title (e.g. Data Structures)
    val instructor: String = "", // Lecturer name or Student group
    val location: String = "", // Venue (e.g. Hall 3B, Science Lab)
    val dayOfWeek: Int, // 1 (Mon) - 7 (Sun)
    val startTime: String, // "09:00"
    val endTime: String, // "10:30"
    val colorHex: String = "#2563EB",
    val category: String = "Lecture", // Lecture, Lab, Tutorial, Seminar, Exam
    val notes: String = "",
    val role: String = "STUDENT", // "STUDENT" vs "TEACHER"
    val customReminderMinutes: Int? = null, // Backward compatibility
    val customReminderSeconds: Int? = null, // Custom seconds before session (choices of seconds, minutes, hours)
    val isMorningBriefingEnabled: Boolean = true, // Early morning alert before first class
    val isPreSessionReminderEnabled: Boolean = true, // Advance alert before session
    val notifyOnSessionStart: Boolean = true // Alert when the session is starting/started
) {
    // Convenient aliases
    val unitCode: String get() = courseCode
    val unitName: String get() = courseName
    val venue: String get() = location

    fun totalReminderSeconds(defaultSeconds: Int = 1800): Int {
        return customReminderSeconds ?: ((customReminderMinutes?.times(60)) ?: defaultSeconds)
    }

    fun formattedReminderOffset(defaultSeconds: Int = 1800): String {
        val sec = totalReminderSeconds(defaultSeconds)
        return when {
            sec % 3600 == 0 && sec >= 3600 -> "${sec / 3600} hr"
            sec % 60 == 0 && sec >= 60 -> "${sec / 60} min"
            sec >= 60 -> "${sec / 60}m ${sec % 60}s"
            else -> "$sec sec"
        }
    }

    fun dayName(): String {
        return when (dayOfWeek) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            7 -> "Sunday"
            else -> "Day $dayOfWeek"
        }
    }

    fun shortDayName(): String {
        return when (dayOfWeek) {
            1 -> "Mon"
            2 -> "Tue"
            3 -> "Wed"
            4 -> "Thu"
            5 -> "Fri"
            6 -> "Sat"
            7 -> "Sun"
            else -> "Day"
        }
    }

    fun startMinutes(): Int {
        val parts = startTime.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return h * 60 + m
    }

    fun endMinutes(): Int {
        val parts = endTime.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return h * 60 + m
    }
}
