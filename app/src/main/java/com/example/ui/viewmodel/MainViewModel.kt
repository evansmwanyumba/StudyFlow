package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.SampleTimetables
import com.example.ai.TimetableAiParser
import com.example.data.database.AppDatabase
import com.example.data.model.DayTask
import com.example.data.model.TaskMode
import com.example.data.model.TaskPriority
import com.example.data.model.TimetableClass
import com.example.data.repository.TaskRepository
import com.example.data.repository.TimetableRepository
import com.example.reminder.ReminderManager
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    AGENDA,
    TIMETABLE,
    TASKS,
    REMINDERS
}

enum class TaskFilter {
    ALL,
    STUDENT,
    GENERAL
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
    private val reminderManager = ReminderManager(application)
    private val aiParser = TimetableAiParser()

    val allClasses: StateFlow<List<TimetableClass>> = timetableRepository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<DayTask>> = taskRepository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(AppTab.AGENDA)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _selectedDayOfWeek = MutableStateFlow(DateUtils.currentDayOfWeek())
    val selectedDayOfWeek: StateFlow<Int> = _selectedDayOfWeek.asStateFlow()

    private val _selectedDate = MutableStateFlow(DateUtils.todayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _taskFilter = MutableStateFlow(TaskFilter.ALL)
    val taskFilter: StateFlow<TaskFilter> = _taskFilter.asStateFlow()

    private val _aiUploadState = MutableStateFlow<AiUploadState>(AiUploadState.Idle)
    val aiUploadState: StateFlow<AiUploadState> = _aiUploadState.asStateFlow()

    private val _morningAlertEnabled = MutableStateFlow(true)
    val morningAlertEnabled: StateFlow<Boolean> = _morningAlertEnabled.asStateFlow()

    private val _sessionAlertEnabled = MutableStateFlow(true)
    val sessionAlertEnabled: StateFlow<Boolean> = _sessionAlertEnabled.asStateFlow()

    init {
        // Seed sample timetable if first launch
        viewModelScope.launch {
            val existing = timetableRepository.getAllClassesList()
            if (existing.isEmpty()) {
                timetableRepository.insertClasses(SampleTimetables.ComputerScience)
                generateWeeklyTasks(SampleTimetables.ComputerScience)
            }
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

    fun setTaskFilter(filter: TaskFilter) {
        _taskFilter.value = filter
    }

    fun toggleMorningAlert(enabled: Boolean) {
        _morningAlertEnabled.value = enabled
        refreshReminders()
    }

    fun toggleSessionAlert(enabled: Boolean) {
        _sessionAlertEnabled.value = enabled
        refreshReminders()
    }

    fun refreshReminders() {
        viewModelScope.launch {
            val todayClasses = timetableRepository.getClassesForDayList(DateUtils.currentDayOfWeek())
            if (_morningAlertEnabled.value || _sessionAlertEnabled.value) {
                reminderManager.scheduleTodayClassReminders(todayClasses)
            }
        }
    }

    fun triggerTestNotification(isMorningBriefing: Boolean) {
        reminderManager.sendInstantTestNotification(isMorningBriefing)
    }

    /**
     * Parse timetable from an uploaded photo or screenshot
     */
    fun uploadTimetableImage(bitmap: Bitmap, replaceExisting: Boolean = false) {
        viewModelScope.launch {
            _aiUploadState.value = AiUploadState.Loading
            val result = aiParser.parseFromImage(bitmap)
            result.onSuccess { classes ->
                if (replaceExisting) {
                    timetableRepository.clearAll()
                }
                timetableRepository.insertClasses(classes)
                generateWeeklyTasks(classes)
                _aiUploadState.value = AiUploadState.Success("Imported ${classes.size} class sessions!", classes.size)
                refreshReminders()
            }.onFailure { err ->
                _aiUploadState.value = AiUploadState.Error(err.message ?: "Failed to parse timetable")
            }
        }
    }

    /**
     * Parse timetable from pasted syllabus or schedule text
     */
    fun uploadTimetableText(text: String, replaceExisting: Boolean = false) {
        viewModelScope.launch {
            _aiUploadState.value = AiUploadState.Loading
            val result = aiParser.parseFromText(text)
            result.onSuccess { classes ->
                if (replaceExisting) {
                    timetableRepository.clearAll()
                }
                timetableRepository.insertClasses(classes)
                generateWeeklyTasks(classes)
                _aiUploadState.value = AiUploadState.Success("Imported ${classes.size} classes from schedule!", classes.size)
                refreshReminders()
            }.onFailure { err ->
                _aiUploadState.value = AiUploadState.Error(err.message ?: "Failed to parse timetable text")
            }
        }
    }

    fun loadPresetTimetable(classes: List<TimetableClass>, replaceExisting: Boolean = true) {
        viewModelScope.launch {
            _aiUploadState.value = AiUploadState.Loading
            if (replaceExisting) {
                timetableRepository.clearAll()
            }
            timetableRepository.insertClasses(classes)
            generateWeeklyTasks(classes)
            _aiUploadState.value = AiUploadState.Success("Loaded ${classes.size} classes!", classes.size)
            refreshReminders()
        }
    }

    fun resetAiUploadState() {
        _aiUploadState.value = AiUploadState.Idle
    }

    /**
     * Organizes timetable classes into actionable day-to-day tasks
     * for the current week (both morning 2hr prep and session tasks)
     */
    fun generateWeeklyTasks(classes: List<TimetableClass> = allClasses.value) {
        viewModelScope.launch {
            for (day in 1..7) {
                val dateStr = DateUtils.getDateStringForDayOfWeek(day)
                val dayClasses = classes.filter { it.dayOfWeek == day }.sortedBy { it.startMinutes() }
                if (dayClasses.isEmpty()) continue

                // Earliest class morning prep task
                val firstClass = dayClasses.firstOrNull()
                if (firstClass != null) {
                    val prepTime = calculateEarlyTime(firstClass.startTime, 120)
                    taskRepository.insertTask(
                        DayTask(
                            title = "🌅 Morning Prep (2h before): ${firstClass.courseCode}",
                            description = "Review slides & notes for ${firstClass.courseName} starting at ${firstClass.startTime} in ${firstClass.location.ifBlank { "TBD" }}",
                            date = dateStr,
                            time = prepTime,
                            priority = TaskPriority.HIGH,
                            mode = TaskMode.STUDENT,
                            linkedClassId = firstClass.id,
                            linkedCourseCode = firstClass.courseCode,
                            category = "Lecture Prep"
                        )
                    )
                }

                // Individual session task
                for (c in dayClasses) {
                    val preSessionTime = calculateEarlyTime(c.startTime, 30)
                    taskRepository.insertTask(
                        DayTask(
                            title = "📚 Attend ${c.courseCode}: ${c.courseName}",
                            description = "${c.category} session with ${c.instructor.ifBlank { "Instructor" }} at ${c.location.ifBlank { "Campus" }} (${c.startTime} - ${c.endTime})",
                            date = dateStr,
                            time = c.startTime,
                            priority = TaskPriority.MEDIUM,
                            mode = TaskMode.STUDENT,
                            linkedClassId = c.id,
                            linkedCourseCode = c.courseCode,
                            category = c.category
                        )
                    )
                }
            }
        }
    }

    private fun calculateEarlyTime(time: String, minutesBefore: Int): String {
        val parts = time.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        var total = h * 60 + m - minutesBefore
        if (total < 0) total = 0
        val targetH = total / 60
        val targetM = total % 60
        return String.format("%02d:%02d", targetH, targetM)
    }

    // --- Timetable CRUD ---
    fun addClass(timetableClass: TimetableClass) {
        viewModelScope.launch {
            val id = timetableRepository.insertClass(timetableClass)
            val updated = timetableClass.copy(id = id)
            // Generate corresponding task
            val dateStr = DateUtils.getDateStringForDayOfWeek(updated.dayOfWeek)
            taskRepository.insertTask(
                DayTask(
                    title = "📚 Attend ${updated.courseCode}: ${updated.courseName}",
                    description = "${updated.category} at ${updated.location} (${updated.startTime} - ${updated.endTime})",
                    date = dateStr,
                    time = updated.startTime,
                    priority = TaskPriority.MEDIUM,
                    mode = TaskMode.STUDENT,
                    linkedClassId = updated.id,
                    linkedCourseCode = updated.courseCode,
                    category = updated.category
                )
            )
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

    // --- Task CRUD ---
    fun addTask(task: DayTask) {
        viewModelScope.launch {
            taskRepository.insertTask(task)
        }
    }

    fun updateTask(task: DayTask) {
        viewModelScope.launch {
            taskRepository.updateTask(task)
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
}
