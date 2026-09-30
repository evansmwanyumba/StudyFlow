package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayTask
import com.example.data.model.TaskMode
import com.example.ui.viewmodel.TaskFilter
import com.example.util.DateUtils

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<DayTask>,
    currentFilter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit,
    onToggleComplete: (Long, Boolean) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onOpenAddTaskDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var filterOnlyToday by remember { mutableStateOf(false) }

    val filteredByMode = when (currentFilter) {
        TaskFilter.ALL -> tasks
        TaskFilter.STUDENT -> tasks.filter { it.mode == TaskMode.STUDENT }
        TaskFilter.GENERAL -> tasks.filter { it.mode == TaskMode.GENERAL }
    }

    val finalTasks = if (filterOnlyToday) {
        filteredByMode.filter { it.date == DateUtils.todayDateString() }
    } else {
        filteredByMode
    }

    val completedCount = finalTasks.count { it.isCompleted }
    val progress = if (finalTasks.isNotEmpty()) completedCount.toFloat() / finalTasks.size else 0f

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Day-to-Day Tasks",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Student course prep & personal day tasks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onOpenAddTaskDialog,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("tasks_add_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Task", fontSize = 12.sp)
                    }
                }
            }

            // Primary Mode Selector: ALL | STUDENT MODE | GENERAL DAY
            item {
                SecondaryTabRow(
                    selectedTabIndex = when (currentFilter) {
                        TaskFilter.ALL -> 0
                        TaskFilter.STUDENT -> 1
                        TaskFilter.GENERAL -> 2
                    }
                ) {
                    Tab(
                        selected = currentFilter == TaskFilter.ALL,
                        onClick = { onFilterChange(TaskFilter.ALL) },
                        text = { Text("🌟 All Tasks (${tasks.size})") },
                        modifier = Modifier.testTag("tab_tasks_all")
                    )
                    Tab(
                        selected = currentFilter == TaskFilter.STUDENT,
                        onClick = { onFilterChange(TaskFilter.STUDENT) },
                        text = { Text("🎓 Student Mode") },
                        modifier = Modifier.testTag("tab_tasks_student")
                    )
                    Tab(
                        selected = currentFilter == TaskFilter.GENERAL,
                        onClick = { onFilterChange(TaskFilter.GENERAL) },
                        text = { Text("📋 General Day") },
                        modifier = Modifier.testTag("tab_tasks_general")
                    )
                }
            }

            // Mode Explainer Card
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentFilter == TaskFilter.STUDENT)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else if (currentFilter == TaskFilter.GENERAL)
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (currentFilter) {
                                TaskFilter.STUDENT -> Icons.Default.School
                                TaskFilter.GENERAL -> Icons.AutoMirrored.Filled.Assignment
                                else -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = when (currentFilter) {
                                    TaskFilter.STUDENT -> "🎓 Student Mode Active"
                                    TaskFilter.GENERAL -> "📋 General Day Tasks Active"
                                    else -> "🌟 Unified Academic & Personal Agenda"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (currentFilter) {
                                    TaskFilter.STUDENT -> "Timetable 2h preps, assignments, exam reviews, & lecture notes."
                                    TaskFilter.GENERAL -> "Personal day tasks apart from school: chores, workouts, errands, habits."
                                    else -> "Showing both academic timetable tasks and personal day tasks seamlessly."
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Date toggle chip & progress
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (filterOnlyToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { filterOnlyToday = !filterOnlyToday }
                            .testTag("filter_only_today_chip")
                    ) {
                        Text(
                            text = if (filterOnlyToday) "Showing Today Only" else "Showing All Dates",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (filterOnlyToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "$completedCount / ${finalTasks.size} Done (${(progress * 100).toInt()}%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }

            // Task List
            if (finalTasks.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No tasks found in this view.",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (currentFilter == TaskFilter.GENERAL)
                                    "Create day-to-day personal tasks such as grocery shopping, workouts, or chores."
                                else
                                    "Tap Add Task to create an assignment, lecture prep, or personal day task!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = onOpenAddTaskDialog) {
                                Text("Create Task")
                            }
                        }
                    }
                }
            } else {
                items(finalTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggleComplete = { onToggleComplete(task.id, it) },
                        onDelete = { onDeleteTask(task.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onOpenAddTaskDialog,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("tasks_fab_add")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Task")
        }
    }
}
