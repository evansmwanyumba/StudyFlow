package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DayTask
import com.example.data.model.TimetableClass
import com.example.ui.dialogs.AddEditClassDialog
import com.example.ui.dialogs.AddEditTaskDialog
import com.example.ui.dialogs.AddExamDialog
import com.example.ui.dialogs.UploadTimetableDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TeacherModeScreen
import com.example.ui.theme.StudyFlowTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel
import com.example.util.DateUtils

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StudyFlowTheme {
                val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
                val allClasses by viewModel.allClasses.collectAsStateWithLifecycle()
                val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
                val allExams by viewModel.allExams.collectAsStateWithLifecycle()
                val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
                val selectedDayOfWeek by viewModel.selectedDayOfWeek.collectAsStateWithLifecycle()
                val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
                val aiUploadState by viewModel.aiUploadState.collectAsStateWithLifecycle()

                // Dialog states
                var showUploadDialog by remember { mutableStateOf(false) }
                var uploadDialogRole by remember { mutableStateOf("STUDENT") }
                var showAddClassDialog by remember { mutableStateOf(false) }
                var addClassDialogRole by remember { mutableStateOf("STUDENT") }
                var showAddTaskDialog by remember { mutableStateOf(false) }
                var showAddExamDialog by remember { mutableStateOf(false) }
                var editingClass by remember { mutableStateOf<TimetableClass?>(null) }
                var editingTask by remember { mutableStateOf<DayTask?>(null) }

                // System back button returns to Student tab if on other tabs
                if (currentTab != AppTab.STUDENT) {
                    BackHandler {
                        viewModel.setTab(AppTab.STUDENT)
                    }
                }

                val todayIsoDay = DateUtils.currentDayOfWeek()
                val todayClasses = allClasses.filter { it.dayOfWeek == todayIsoDay }
                val todayTasks = allTasks.filter { it.date == DateUtils.todayDateString() }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isExpanded = maxWidth >= 720.dp

                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when (currentTab) {
                                                AppTab.STUDENT -> Icons.Default.School
                                                AppTab.GENERAL_TASKS -> Icons.Default.TaskAlt
                                                AppTab.TEACHER -> Icons.Default.CoPresent
                                                AppTab.REMINDERS -> Icons.Default.Alarm
                                                AppTab.SETTINGS -> Icons.Default.Settings
                                            },
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                                        Text(
                                            text = when (currentTab) {
                                                AppTab.STUDENT -> "Student Mode"
                                                AppTab.GENERAL_TASKS -> "Daily Tasks"
                                                AppTab.TEACHER -> "Lecturer Mode"
                                                AppTab.REMINDERS -> "Alarms & Alerts"
                                                AppTab.SETTINGS -> "Settings"
                                            },
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 19.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                actions = {
                                    if (currentTab == AppTab.STUDENT || currentTab == AppTab.TEACHER) {
                                        IconButton(
                                            onClick = {
                                                uploadDialogRole = if (currentTab == AppTab.TEACHER) "TEACHER" else "STUDENT"
                                                showUploadDialog = true
                                            },
                                            modifier = Modifier.testTag("appbar_scan_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = "Scan Timetable",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        },
                        bottomBar = {
                            if (!isExpanded) {
                                NavigationBar(
                                    modifier = Modifier
                                        .windowInsetsPadding(WindowInsets.navigationBars)
                                        .testTag("bottom_nav_bar")
                                ) {
                                    NavigationBarItem(
                                        selected = currentTab == AppTab.STUDENT,
                                        onClick = { viewModel.setTab(AppTab.STUDENT) },
                                        icon = {
                                            Icon(
                                                if (currentTab == AppTab.STUDENT) Icons.Default.School else Icons.Outlined.School,
                                                contentDescription = "Student Mode"
                                            )
                                        },
                                        label = { Text("Student", fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_tab_student")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == AppTab.GENERAL_TASKS,
                                        onClick = { viewModel.setTab(AppTab.GENERAL_TASKS) },
                                        icon = {
                                            val pending = allTasks.count { !it.isCompleted }
                                            BadgedBox(
                                                badge = {
                                                    if (pending > 0) {
                                                        Badge { Text("$pending") }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    if (currentTab == AppTab.GENERAL_TASKS) Icons.Default.TaskAlt else Icons.Outlined.TaskAlt,
                                                    contentDescription = "Daily Tasks"
                                                )
                                            }
                                        },
                                        label = { Text("Daily Tasks", fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_tab_daily_tasks")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == AppTab.TEACHER,
                                        onClick = { viewModel.setTab(AppTab.TEACHER) },
                                        icon = {
                                            Icon(
                                                if (currentTab == AppTab.TEACHER) Icons.Default.CoPresent else Icons.Outlined.CoPresent,
                                                contentDescription = "Lecturer"
                                            )
                                        },
                                        label = { Text("Lecturer", fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_tab_lecturer")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == AppTab.REMINDERS,
                                        onClick = { viewModel.setTab(AppTab.REMINDERS) },
                                        icon = {
                                            Icon(
                                                if (currentTab == AppTab.REMINDERS) Icons.Default.Alarm else Icons.Outlined.Alarm,
                                                contentDescription = "Alarms"
                                            )
                                        },
                                        label = { Text("Alarms", fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_tab_reminders")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == AppTab.SETTINGS,
                                        onClick = { viewModel.setTab(AppTab.SETTINGS) },
                                        icon = {
                                            Icon(
                                                if (currentTab == AppTab.SETTINGS) Icons.Default.Settings else Icons.Outlined.Settings,
                                                contentDescription = "Settings"
                                            )
                                        },
                                        label = { Text("Settings", fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_tab_settings")
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (isExpanded) {
                                NavigationRail {
                                    NavigationRailItem(
                                        selected = currentTab == AppTab.STUDENT,
                                        onClick = { viewModel.setTab(AppTab.STUDENT) },
                                        icon = { Icon(Icons.Default.School, contentDescription = "Student Mode") },
                                        label = { Text("Student") }
                                    )
                                    NavigationRailItem(
                                        selected = currentTab == AppTab.GENERAL_TASKS,
                                        onClick = { viewModel.setTab(AppTab.GENERAL_TASKS) },
                                        icon = { Icon(Icons.Default.TaskAlt, contentDescription = "Daily Tasks") },
                                        label = { Text("Daily Tasks") }
                                    )
                                    NavigationRailItem(
                                        selected = currentTab == AppTab.TEACHER,
                                        onClick = { viewModel.setTab(AppTab.TEACHER) },
                                        icon = { Icon(Icons.Default.CoPresent, contentDescription = "Lecturer") },
                                        label = { Text("Lecturer") }
                                    )
                                    NavigationRailItem(
                                        selected = currentTab == AppTab.REMINDERS,
                                        onClick = { viewModel.setTab(AppTab.REMINDERS) },
                                        icon = { Icon(Icons.Default.Alarm, contentDescription = "Alarms") },
                                        label = { Text("Alarms") }
                                    )
                                    NavigationRailItem(
                                        selected = currentTab == AppTab.SETTINGS,
                                        onClick = { viewModel.setTab(AppTab.SETTINGS) },
                                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                        label = { Text("Settings") }
                                    )
                                }
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                when (currentTab) {
                                    AppTab.STUDENT -> HomeScreen(
                                        todayClasses = todayClasses,
                                        todayTasks = todayTasks,
                                        exams = allExams,
                                        onToggleTaskComplete = { id, comp -> viewModel.toggleTaskComplete(id, comp) },
                                        onDeleteTask = { id -> viewModel.deleteTask(id) },
                                        onDeleteExam = { id -> viewModel.deleteExam(id) },
                                        onOpenUploadDialog = {
                                            uploadDialogRole = "STUDENT"
                                            showUploadDialog = true
                                        },
                                        onOpenAddClassDialog = {
                                            addClassDialogRole = "STUDENT"
                                            showAddClassDialog = true
                                        },
                                        onOpenAddTaskDialog = { showAddTaskDialog = true },
                                        onOpenAddExamDialog = { showAddExamDialog = true },
                                        onNavigateTab = { viewModel.setTab(it) }
                                    )
                                    AppTab.GENERAL_TASKS -> TasksScreen(
                                        tasks = allTasks,
                                        onToggleComplete = { id, comp -> viewModel.toggleTaskComplete(id, comp) },
                                        onDeleteTask = { id -> viewModel.deleteTask(id) },
                                        onOpenAddTaskDialog = { showAddTaskDialog = true },
                                        onCleanPassedTasks = { viewModel.checkAndDiscardPassedTasks() }
                                    )
                                    AppTab.TEACHER -> TeacherModeScreen(
                                        teachingClasses = allClasses.filter { it.role == "TEACHER" },
                                        selectedDayOfWeek = selectedDayOfWeek,
                                        defaultTeacherOffset = userSettings.teacherOffsetMinutes,
                                        onSelectDay = { viewModel.selectDayOfWeek(it) },
                                        onOpenUploadDialog = {
                                            uploadDialogRole = "TEACHER"
                                            showUploadDialog = true
                                        },
                                        onOpenAddClassDialog = {
                                            addClassDialogRole = "TEACHER"
                                            showAddClassDialog = true
                                        },
                                        onEditClass = { editingClass = it },
                                        onDeleteClass = { viewModel.deleteClass(it) }
                                    )
                                    AppTab.REMINDERS -> RemindersScreen(
                                        todayClasses = todayClasses,
                                        userSettings = userSettings,
                                        onTriggerTestAlarm = { isMorning, isSessionStarted ->
                                            viewModel.triggerTestFullScreenAlarm(isMorning, isSessionStarted)
                                        },
                                        onOpenSettings = { viewModel.setTab(AppTab.SETTINGS) }
                                    )
                                    AppTab.SETTINGS -> SettingsScreen(
                                        userSettings = userSettings,
                                        onUpdateMorningOffsetSeconds = { viewModel.updateMorningOffsetSeconds(it) },
                                        onUpdateSessionOffsetSeconds = { viewModel.updateSessionOffsetSeconds(it) },
                                        onUpdateTeacherOffsetSeconds = { viewModel.updateTeacherOffsetSeconds(it) },
                                        onUpdateNotifyOnSessionStart = { viewModel.updateNotifyOnSessionStart(it) },
                                        onUpdateRingtone = { viewModel.updateRingtone(it) },
                                        onUpdateVibrateInSilent = { viewModel.updateVibrateInSilent(it) },
                                        onUpdateAutoDiscard = { viewModel.updateAutoDiscard(it) },
                                        onUpdateGeminiKey = { viewModel.updateGeminiApiKey(it) },
                                        onCleanUpAllData = { viewModel.clearAllData() }
                                    )
                                }
                            }
                        }

                        // Dialogs
                        if (showUploadDialog) {
                            UploadTimetableDialog(
                                viewModel = viewModel,
                                aiUploadState = aiUploadState,
                                initialRole = uploadDialogRole,
                                onDismiss = { showUploadDialog = false }
                            )
                        }

                        if (showAddClassDialog) {
                            AddEditClassDialog(
                                defaultDayOfWeek = selectedDayOfWeek,
                                defaultRole = addClassDialogRole,
                                onSave = { viewModel.addClass(it) },
                                onDismiss = { showAddClassDialog = false }
                            )
                        }

                        if (editingClass != null) {
                            AddEditClassDialog(
                                initialClass = editingClass,
                                defaultDayOfWeek = editingClass!!.dayOfWeek,
                                defaultRole = editingClass!!.role,
                                onSave = { viewModel.updateClass(it) },
                                onDelete = { viewModel.deleteClass(it) },
                                onDismiss = { editingClass = null }
                            )
                        }

                        if (showAddTaskDialog) {
                            AddEditTaskDialog(
                                defaultDate = selectedDate,
                                availableClasses = allClasses,
                                onSave = { viewModel.addTask(it) },
                                onDismiss = { showAddTaskDialog = false }
                            )
                        }

                        if (showAddExamDialog) {
                            AddExamDialog(
                                onSave = { viewModel.addExam(it) },
                                onDismiss = { showAddExamDialog = false }
                            )
                        }

                        if (editingTask != null) {
                            AddEditTaskDialog(
                                initialTask = editingTask,
                                defaultDate = editingTask!!.date,
                                availableClasses = allClasses,
                                onSave = { viewModel.updateTask(it) },
                                onDelete = { viewModel.deleteTask(it) },
                                onDismiss = { editingTask = null }
                            )
                        }
                    }
                }
            }
        }
    }
}
