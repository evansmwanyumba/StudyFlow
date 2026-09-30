package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable_classes")
data class TimetableClass(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseCode: String,
    val courseName: String,
    val instructor: String = "",
    val location: String = "",
    val dayOfWeek: Int, // 1 (Mon) - 7 (Sun)
    val startTime: String, // "09:00"
    val endTime: String, // "10:30"
    val colorHex: String = "#3B82F6",
    val category: String = "Lecture", // Lecture, Lab, Tutorial, Seminar, Exam
    val notes: String = "",
    val isMorningBriefingEnabled: Boolean = true, // 2h before first class
    val isPreSessionReminderEnabled: Boolean = true // 30m before class
) {
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
