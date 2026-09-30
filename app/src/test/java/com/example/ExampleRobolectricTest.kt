package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DayTask
import com.example.data.model.TaskMode
import com.example.data.model.TaskPriority
import com.example.data.model.TimetableClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("StudyFlow", appName)
    }

    @Test
    fun `test timetable class minutes and reminder offsets`() {
        val sample = TimetableClass(
            courseCode = "CS 201",
            courseName = "Data Structures",
            dayOfWeek = 1,
            startTime = "08:30",
            endTime = "10:00"
        )

        // 8:30 = 8 * 60 + 30 = 510 minutes
        assertEquals(510, sample.startMinutes())
        assertEquals(600, sample.endMinutes())

        // Morning 2-hour (120 min) early alert: 510 - 120 = 390 min -> 06:30
        val morningAlertMins = sample.startMinutes() - 120
        assertEquals(390, morningAlertMins)
        assertEquals("06:30", String.format("%02d:%02d", morningAlertMins / 60, morningAlertMins % 60))

        // Pre-session 30 min alert: 510 - 30 = 480 min -> 08:00
        val sessionAlertMins = sample.startMinutes() - 30
        assertEquals(480, sessionAlertMins)
        assertEquals("08:00", String.format("%02d:%02d", sessionAlertMins / 60, sessionAlertMins % 60))
    }

    @Test
    fun `test student mode vs general day task separation`() {
        val studentTask = DayTask(
            title = "Prepare lab report",
            date = "2026-09-30",
            mode = TaskMode.STUDENT,
            linkedCourseCode = "CS 201",
            category = "Lecture Prep"
        )

        val generalTask = DayTask(
            title = "Pick up groceries and workout",
            date = "2026-09-30",
            mode = TaskMode.GENERAL,
            priority = TaskPriority.HIGH,
            category = "Personal"
        )

        assertEquals(TaskMode.STUDENT, studentTask.mode)
        assertEquals("CS 201", studentTask.linkedCourseCode)

        assertEquals(TaskMode.GENERAL, generalTask.mode)
        assertEquals(null, generalTask.linkedCourseCode)
    }
}
