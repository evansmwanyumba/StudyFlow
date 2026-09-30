package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskMode {
    STUDENT,
    GENERAL
}

enum class TaskPriority {
    HIGH,
    MEDIUM,
    LOW
}

@Entity(tableName = "day_tasks")
data class DayTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val date: String, // "YYYY-MM-DD"
    val time: String? = null, // "14:00"
    val isCompleted: Boolean = false,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val mode: TaskMode = TaskMode.STUDENT,
    val linkedClassId: Long? = null,
    val linkedCourseCode: String? = null,
    val category: String = "General", // "Lecture Prep", "Assignment", "Study", "Errand", "Personal", "Health", "Work"
    val reminderMinutesBefore: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)
