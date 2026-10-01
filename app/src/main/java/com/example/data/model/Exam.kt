package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exams")
data class Exam(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val unitCode: String, // Unit / Course code (e.g. CS201)
    val unitTitle: String, // Course name (e.g. Algorithms Final)
    val examDate: String, // "YYYY-MM-DD"
    val examTime: String = "", // "09:00" or empty if time not specified
    val venue: String = "", // Examination Hall / Room
    val reminderFrequency: String = "Daily", // "Daily", "Every 2 Days", "Weekly", "1 Day Before"
    val studyNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isExamTimeSpecified(): Boolean = examTime.isNotBlank()

    fun examStartMinutes(): Int? {
        if (!isExamTimeSpecified()) return null
        val parts = examTime.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val m = parts.getOrNull(1)?.toIntOrNull() ?: return null
        return h * 60 + m
    }
}
