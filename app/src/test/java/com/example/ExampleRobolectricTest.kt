package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DayTask
import com.example.data.model.Exam
import com.example.data.model.TaskMode
import com.example.data.model.TaskPriority
import com.example.data.model.TimetableClass
import com.example.data.repository.SettingsRepository
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
            location = "Hall 302",
            dayOfWeek = 1,
            startTime = "08:30",
            endTime = "10:00",
            role = "STUDENT"
        )

        // 8:30 = 8 * 60 + 30 = 510 minutes
        assertEquals(510, sample.startMinutes())
        assertEquals(600, sample.endMinutes())
        assertEquals("Hall 302", sample.venue)

        // Custom reminder offset test (e.g. 15 min or 60 min)
        val customOffset = 45
        val alertMins = sample.startMinutes() - customOffset
        assertEquals(465, alertMins)
        assertEquals("07:45", String.format("%02d:%02d", alertMins / 60, alertMins % 60))
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

    @Test
    fun `test exam priority 1 and time calculation`() {
        val exam = Exam(
            unitCode = "MATH 240",
            unitTitle = "Discrete Mathematics Final",
            examDate = "2026-10-15",
            examTime = "14:00",
            venue = "Great Examination Hall",
            reminderFrequency = "Daily"
        )

        assertTrue(exam.isExamTimeSpecified())
        assertEquals(840, exam.examStartMinutes()) // 14:00 = 840 mins
    }

    @Test
    fun `test settings repository customization`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val settingsRepo = SettingsRepository(context)

        assertEquals(120, settingsRepo.settings.value.morningOffsetMinutes)
        assertEquals(30, settingsRepo.settings.value.sessionOffsetMinutes)
        assertTrue(settingsRepo.settings.value.vibrateInSilentMode)

        settingsRepo.updateMorningOffset(90)
        settingsRepo.updateSessionOffset(15)
        settingsRepo.updateRingtone("School Bell")

        assertEquals(90, settingsRepo.settings.value.morningOffsetMinutes)
        assertEquals(15, settingsRepo.settings.value.sessionOffsetMinutes)
        assertEquals("School Bell", settingsRepo.settings.value.selectedRingtone)
    }
}
