package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.TimetableAiParser
import com.example.data.database.AppDatabase
import com.example.data.model.DayTask
import com.example.data.model.Exam
import com.example.data.model.TaskMode
import com.example.data.model.TaskPriority
import com.example.data.model.TimetableClass
import com.example.data.repository.ExamRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.TaskRepository
import com.example.data.repository.TimetableRepository
import com.example.data.repository.UserSettings
import com.example.reminder.ReminderManager
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppTab {
    STUDENT,
    GENERAL_TASKS,
    TEACHER,
    REMINDERS,
    SETTINGS
}

sealed interface AiUploadState {
    object Idle : AiUploadState
    object Loading : AiUploadState
    data class Success(val message: String, val count: Int) : AiUploadState
    data class Error(val error: String) : AiUploadState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val timetableRepository = TimetableRepository(db.timetableDao())
    private val taskRepository = TaskRepository(db.dayTaskDao())
    private val examRepository = ExamRepository(db.examDao())
    private val settingsRepository = SettingsRepository(application)
    private val reminderManager = ReminderManager(application)
    private val aiParser = TimetableAiParser()

    val allClasses: StateFlow<List<TimetableClass>> = timetableRepository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<DayTask>> = taskRepository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExams: StateFlow<List<Exam>> = examRepository.allExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSettings: StateFlow<UserSettings> = settingsRepository.settings

    private val _currentTab = MutableStateFlow(AppTab.STUDENT)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _selectedDayOfWeek = MutableStateFlow(DateUtils.currentDayOfWeek())
    val selectedDayOfWeek: StateFlow<Int> = _selectedDayOfWeek.asStateFlow()

    private val _selectedDate = MutableStateFlow(DateUtils.todayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _aiUploadState = MutableStateFlow<AiUploadState>(AiUploadState.Idle)
    val aiUploadState: StateFlow<AiUploadState> = _aiUploadState.asStateFlow()

    init {
        viewModelScope.launch {
            checkAndDiscardPassedTasks()
            evaluateExamsAndVoidCollidingTasks()
            refreshReminders()
        }
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun selectDayOfWeek(day: Int) {
        _selectedDayOfWeek.value = day
        _selectedDate.value = DateUtils.getDateStringForDayOfWeek(day)
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    // --- Settings Updates (Seconds / Minutes / Hours) ---
    fun updateMorningOffsetSeconds(seconds: Int) {
        settingsRepository.updateMorningOffsetSeconds(seconds)
        refreshReminders()
    }

    fun updateSessionOffsetSeconds(seconds: Int) {
        settingsRepository.updateSessionOffsetSeconds(seconds)
        refreshReminders()
    }

    fun updateTeacherOffsetSeconds(seconds: Int) {
        settingsRepository.updateTeacherOffsetSeconds(seconds)
        refreshReminders()
    }

    fun updateNotifyOnSessionStart(enabled: Boolean) {
        settingsRepository.updateNotifyOnSessionStart(enabled)
        refreshReminders()
    }

    fun updateRingtone(ringtone: String) {
        settingsRepository.updateRingtone(ringtone)
    }

    fun updateVibrateInSilent(enabled: Boolean) {
        settingsRepository.updateVibrateInSilent(enabled)
    }

    fun updateAutoDiscard(enabled: Boolean) {
        settingsRepository.updateAutoDiscard(enabled)
        if (enabled) {
            checkAndDiscardPassedTasks()
        }
    }

    fun updateGeminiApiKey(key: String) {
        settingsRepository.updateCustomGeminiKey(key)
    }

    fun clearAllData() {
        viewModelScope.launch {
            timetableRepository.clearAll()
            taskRepository.clearAllTasks()
            examRepository.clearAll()
            refreshReminders()
        }
    }

    fun checkAndDiscardPassedTasks() {
        if (!userSettings.value.autoDiscardPassedTasks) return
        viewModelScope.launch {
            val today = DateUtils.todayDateString()
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val nowTimeStr = timeFormat.format(Date())
            val nowParts = nowTimeStr.split(":")
            val nowMinutes = (nowParts[0].toIntOrNull() ?: 0) * 60 + (nowParts[1].toIntOrNull() ?: 0)

            val tasks = taskRepository.getAllUndiscardedTasksList()
            for (task in tasks) {
                if (task.date < today) {
                    taskRepository.markTaskPassedAndDiscarded(task.id)
                } else if (task.date == today && task.time != null) {
                    val taskMin = task.taskMinutes() ?: continue
                    if (taskMin < nowMinutes && !task.isCompleted) {
                        taskRepository.markTaskPassedAndDiscarded(task.id)
                    }
                }
            }
        }
    }

    fun evaluateExamsAndVoidCollidingTasks() {
        viewModelScope.launch {
            val exams = examRepository.getAllExamsList()
            for (exam in exams) {
                if (exam.isExamTimeSpecified()) {
                    val examStart = exam.examStartMinutes() ?: continue
                    val examEnd = examStart + 180
                    val dayTasks = taskRepository.getTasksForDateList(exam.examDate)
                    for (task in dayTasks) {
                        val taskMin = task.taskMinutes()
                        if (taskMin != null && taskMin in (examStart - 30)..examEnd) {
                            taskRepository.voidTaskDueToExam(
                                task.id,
                                "Voided: Collides with Exam for ${exam.unitCode} (${exam.examTime}) in ${exam.venue}"
                            )
                        }
                    }
                }
            }
        }
    }

    fun uploadTimetableImage(bitmap: Bitmap, role: String = "STUDENT", replaceExisting: Boolean = false) {
        viewModelScope.launch {
            _aiUploadState.value = AiUploadState.Loading
            val customKey = userSettings.value.customGeminiApiKey
            val result = aiParser.parseFromImage(bitmap, role, customKey)
            result.onSuccess { classes ->
                if (replaceExisting) {
                    timetableRepository.clearAll()
                }
                timetableRepository.insertClasses(classes)
                if (role == "STUDENT") {
                    generateWeeklyTasks(classes)
                }
                _aiUploadState.value = AiUploadState.Success("Successfully scanned ${classes.size} sessions!", classes.size)
                refreshReminders()
            }.onFailure { err ->
                _aiUploadState.value = AiUploadState.Error(err.message ?: "Could not parse timetable.")
            }
        }
    }

    fun uploadTimetableText(text: String, role: String = "STUDENT", replaceExisting: Boolean = false) {
        viewModelScope.launch {
            _aiUploadState.value = AiUploadState.Loading
            val customKey = userSettings.value.customGeminiApiKey
            val result = aiParser.parseFromText(text, role, customKey)
            result.onSuccess { classes ->
                if (replaceExisting) {
                    timetableRepository.clearAll()
                }
                timetableRepository.insertClasses(classes)
                if (role == "STUDENT") {
                    generateWeeklyTasks(classes)
                }
                _aiUploadState.value = AiUploadState.Success("Imported ${classes.size} sessions!", classes.size)
                refreshReminders()
            }.onFailure { err ->
                _aiUploadState.value = AiUploadState.Error(err.message ?: "Could not parse text.")
            }
        }
    }

    fun resetAiUploadState() {
        _aiUploadState.value = AiUploadState.Idle
    }

    fun addClass(timetableClass: TimetableClass) {
        viewModelScope.launch {
            val id = timetableRepository.insertClass(timetableClass)
            val updated = timetableClass.copy(id = id)
            if (updated.role == "STUDENT") {
                val dateStr = DateUtils.getDateStringForDayOfWeek(updated.dayOfWeek)
                taskRepository.insertTask(
                    DayTask(
                        title = "📚 Attend ${updated.unitCode}: ${updated.unitName}",
                        description = "${updated.category} at ${updated.venue} (${updated.startTime} - ${updated.endTime})",
                        date = dateStr,
                        time = updated.startTime,
                        priority = TaskPriority.MEDIUM,
                        mode = TaskMode.STUDENT,
                        linkedClassId = updated.id,
                        linkedCourseCode = updated.unitCode,
                        category = updated.category
                    )
                )
            }
            refreshReminders()
        }
    }

    fun updateClass(timetableClass: TimetableClass) {
        viewModelScope.launch {
            timetableRepository.updateClass(timetableClass)
            refreshReminders()
        }
    }

    fun deleteClass(id: Long) {
        viewModelScope.launch {
            timetableRepository.deleteClassById(id)
            refreshReminders()
        }
    }

    fun addExam(exam: Exam) {
        viewModelScope.launch {
            val id = examRepository.insertExam(exam)
            val savedExam = exam.copy(id = id)
            reminderManager.scheduleExamReminder(savedExam)

            taskRepository.insertTask(
                DayTask(
                    title = "📖 Study for Exam: ${savedExam.unitCode}",
                    description = "Upcoming exam on ${savedExam.examDate} at ${savedExam.examTime.ifBlank { "TBD" }} in ${savedExam.venue}. ${savedExam.studyNotes}",
                    date = savedExam.examDate,
                    time = if (savedExam.isExamTimeSpecified()) savedExam.examTime else null,
                    priority = TaskPriority.HIGH,
                    mode = TaskMode.STUDENT,
                    category = "Exam Review"
                )
            )

            evaluateExamsAndVoidCollidingTasks()
        }
    }

    fun deleteExam(id: Long) {
        viewModelScope.launch {
            examRepository.deleteExamById(id)
        }
    }

    fun addTask(task: DayTask) {
        viewModelScope.launch {
            taskRepository.insertTask(task)
            evaluateExamsAndVoidCollidingTasks()
        }
    }

    fun updateTask(task: DayTask) {
        viewModelScope.launch {
            taskRepository.updateTask(task)
            evaluateExamsAndVoidCollidingTasks()
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            taskRepository.deleteTaskById(id)
        }
    }

    fun toggleTaskComplete(id: Long, completed: Boolean) {
        viewModelScope.launch {
            taskRepository.setTaskCompleted(id, completed)
        }
    }

    fun refreshReminders() {
        viewModelScope.launch {
            val todayClasses = timetableRepository.getClassesForDayList(DateUtils.currentDayOfWeek())
            val s = userSettings.value
            reminderManager.scheduleTodayClassReminders(
                todayClasses = todayClasses,
                morningOffsetSeconds = s.morningOffsetSeconds,
                sessionOffsetSeconds = s.sessionOffsetSeconds,
                notifyOnSessionStart = s.notifyOnSessionStart
            )
        }
    }

    fun triggerTestFullScreenAlarm(isMorning: Boolean, isSessionStarted: Boolean = false) {
        reminderManager.triggerTestFullScreenAlarm(isMorning, isSessionStarted)
    }

    fun generateWeeklyTasks(classes: List<TimetableClass> = allClasses.value) {
        viewModelScope.launch {
            val studentClasses = classes.filter { it.role == "STUDENT" }
            val morningOffsetSec = userSettings.value.morningOffsetSeconds

            for (day in 1..7) {
                val dateStr = DateUtils.getDateStringForDayOfWeek(day)
                val dayClasses = studentClasses.filter { it.dayOfWeek == day }.sortedBy { it.startMinutes() }
                if (dayClasses.isEmpty()) continue

                val firstClass = dayClasses.firstOrNull()
                if (firstClass != null && firstClass.isMorningBriefingEnabled) {
                    val prepTime = calculateEarlyTimeSec(firstClass.startTime, morningOffsetSec)
                    taskRepository.insertTask(
                        DayTask(
                            title = "⏰ Wake Up & Get Ready: ${firstClass.unitCode}",
                            description = "Wake up and get ready for ${firstClass.unitName} at ${firstClass.startTime} in ${firstClass.venue}",
                            date = dateStr,
                            time = prepTime,
                            priority = TaskPriority.HIGH,
                            mode = TaskMode.STUDENT,
                            linkedClassId = firstClass.id,
                            linkedCourseCode = firstClass.unitCode,
                            category = "Morning Routine"
                        )
                    )
                }

                for (c in dayClasses) {
                    taskRepository.insertTask(
                        DayTask(
                            title = "📚 Attend ${c.unitCode}: ${c.unitName}",
                            description = "${c.category} with ${c.instructor.ifBlank { "Lecturer" }} at ${c.venue} (${c.startTime} - ${c.endTime})",
                            date = dateStr,
                            time = c.startTime,
                            priority = TaskPriority.MEDIUM,
                            mode = TaskMode.STUDENT,
                            linkedClassId = c.id,
                            linkedCourseCode = c.unitCode,
                            category = c.category
                        )
                    )
                }
            }
            evaluateExamsAndVoidCollidingTasks()
        }
    }

    private fun calculateEarlyTimeSec(time: String, secondsBefore: Int): String {
        val parts = time.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        var totalSec = (h * 60 + m) * 60 - secondsBefore
        if (totalSec < 0) totalSec = 0
        val targetH = (totalSec / 3600) % 24
        val targetM = (totalSec % 3600) / 60
        return String.format("%02d:%02d", targetH, targetM)
    }
}
