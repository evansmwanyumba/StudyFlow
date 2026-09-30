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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Today
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
import androidx.compose.material3.Surface
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
import com.example.ui.dialogs.UploadTimetableDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TimetableScreen
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
                val selectedDayOfWeek by viewModel.selectedDayOfWeek.collectAsStateWithLifecycle()
                val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
                val taskFilter by viewModel.taskFilter.collectAsStateWithLifecycle()
                val aiUploadState by viewModel.aiUploadState.collectAsStateWithLifecycle()
                val morningAlertEnabled by viewModel.morningAlertEnabled.collectAsStateWithLifecycle()
                val sessionAlertEnabled by viewModel.sessionAlertEnabled.collectAsStateWithLifecycle()

                // Dialog states
                var showUploadDialog by remember { mutableStateOf(false) }
                var showAddClassDialog by remember { mutableStateOf(false) }
                var showAddTaskDialog by remember { mutableStateOf(false) }
                var editingClass by remember { mutableStateOf<TimetableClass?>(null) }
                var editingTask by remember { mutableStateOf<DayTask?>(null) }

                // System back button returns to Agenda if on other tabs
                if (currentTab != AppTab.AGENDA) {
                    BackHandler {
                        viewModel.setTab(AppTab.AGENDA)
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
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                                        Text(
                                            text = "StudyFlow",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 19.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = { showUploadDialog = true },
                                        modifier = Modifier.testTag("appbar_upload_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "AI Upload Timetable",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
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
                                        selected = currentTab == AppTab.AGENDA,
                                        onClick = { viewModel.setTab(AppTab.AGENDA) },
                                        icon = {
                                            Icon(
                                                if (currentTab == AppTab.AGENDA) Icons.Default.Today else Icons.Outlined.Today,
                                                contentDescription = "Today"
                                            )
                                        },
                                        label = { Text("Today") },
                                        modifier = Modifier.testTag("nav_tab_agenda")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == AppTab.TIMETABLE,
                                        onClick = { viewModel.setTab(AppTab.TIMETABLE) },
                                        icon = {
                                            BadgedBox(
                                                badge = {
                                                    if (allClasses.isNotEmpty()) {
                                                        Badge { Text("${allClasses.size}") }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    if (currentTab == AppTab.TIMETABLE) Icons.Default.CalendarMonth else Icons.Outlined.CalendarMonth,
                                                    contentDescription = "Timetable"
                                                )
                                            }
                                        },
                                        label = { Text("Timetable") },
                                        modifier = Modifier.testTag("nav_tab_timetable")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == AppTab.TASKS,
                                        onClick = { viewModel.setTab(AppTab.TASKS) },
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
                                                    if (currentTab == AppTab.TASKS) Icons.Default.Checklist else Icons.Outlined.Checklist,
                                                    contentDescription = "Tasks"
                                                )
                                            }
                                        },
                                        label = { Text("Day Tasks") },
                                        modifier = Modifier.testTag("nav_tab_tasks")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == AppTab.REMINDERS,
                                        onClick = { viewModel.setTab(AppTab.REMINDERS) },
                                        icon = {
                                            Icon(
                                                if (currentTab == AppTab.REMINDERS) Icons.Default.Alarm else Icons.Outlined.Alarm,
                                                contentDescription = "Reminders"
                                            )
                                        },
                                        label = { Text("Reminders") },
                                        modifier = Modifier.testTag("nav_tab_reminders")
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
                                        selected = currentTab == AppTab.AGENDA,
                                        onClick = { viewModel.setTab(AppTab.AGENDA) },
                                        icon = { Icon(Icons.Default.Today, contentDescription = "Today") },
                                        label = { Text("Today") }
                                    )
                                    NavigationRailItem(
                                        selected = currentTab == AppTab.TIMETABLE,
                                        onClick = { viewModel.setTab(AppTab.TIMETABLE) },
                                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Timetable") },
                                        label = { Text("Timetable") }
                                    )
                                    NavigationRailItem(
                                        selected = currentTab == AppTab.TASKS,
                                        onClick = { viewModel.setTab(AppTab.TASKS) },
                                        icon = { Icon(Icons.Default.Checklist, contentDescription = "Tasks") },
                                        label = { Text("Tasks") }
                                    )
                                    NavigationRailItem(
                                        selected = currentTab == AppTab.REMINDERS,
                                        onClick = { viewModel.setTab(AppTab.REMINDERS) },
                                        icon = { Icon(Icons.Default.Alarm, contentDescription = "Reminders") },
                                        label = { Text("Reminders") }
                                    )
                                }
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                when (currentTab) {
                                    AppTab.AGENDA -> HomeScreen(
                                        todayClasses = todayClasses,
                                        todayTasks = todayTasks,
                                        currentTaskFilter = taskFilter,
                                        onFilterChange = { viewModel.setTaskFilter(it) },
                                        onToggleTaskComplete = { id, comp -> viewModel.toggleTaskComplete(id, comp) },
                                        onDeleteTask = { id -> viewModel.deleteTask(id) },
                                        onOpenUploadDialog = { showUploadDialog = true },
                                        onOpenAddClassDialog = { showAddClassDialog = true },
                                        onOpenAddTaskDialog = { showAddTaskDialog = true },
                                        onNavigateTab = { viewModel.setTab(it) }
                                    )
                                    AppTab.TIMETABLE -> TimetableScreen(
                                        allClasses = allClasses,
                                        selectedDayOfWeek = selectedDayOfWeek,
                                        onSelectDay = { viewModel.selectDayOfWeek(it) },
                                        onOpenUploadDialog = { showUploadDialog = true },
                                        onOpenAddClassDialog = { showAddClassDialog = true },
                                        onEditClass = { editingClass = it },
                                        onDeleteClass = { viewModel.deleteClass(it) },
                                        onSyncWeekTasks = { viewModel.generateWeeklyTasks() }
                                    )
                                    AppTab.TASKS -> TasksScreen(
                                        tasks = allTasks,
                                        currentFilter = taskFilter,
                                        onFilterChange = { viewModel.setTaskFilter(it) },
                                        onToggleComplete = { id, comp -> viewModel.toggleTaskComplete(id, comp) },
                                        onDeleteTask = { viewModel.deleteTask(it) },
                                        onOpenAddTaskDialog = { showAddTaskDialog = true }
                                    )
                                    AppTab.REMINDERS -> RemindersScreen(
                                        todayClasses = todayClasses,
                                        isMorningAlertEnabled = morningAlertEnabled,
                                        isSessionAlertEnabled = sessionAlertEnabled,
                                        onToggleMorningAlert = { viewModel.toggleMorningAlert(it) },
                                        onToggleSessionAlert = { viewModel.toggleSessionAlert(it) },
                                        onTriggerTestNotification = { viewModel.triggerTestNotification(it) }
                                    )
                                }
                            }
                        }

                        // Dialogs
                        if (showUploadDialog) {
                            UploadTimetableDialog(
                                viewModel = viewModel,
                                aiUploadState = aiUploadState,
                                onDismiss = { showUploadDialog = false }
                            )
                        }

                        if (showAddClassDialog) {
                            AddEditClassDialog(
                                defaultDayOfWeek = selectedDayOfWeek,
                                onSave = { viewModel.addClass(it) },
                                onDismiss = { showAddClassDialog = false }
                            )
                        }

                        if (editingClass != null) {
                            AddEditClassDialog(
                                initialClass = editingClass,
                                defaultDayOfWeek = editingClass!!.dayOfWeek,
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
