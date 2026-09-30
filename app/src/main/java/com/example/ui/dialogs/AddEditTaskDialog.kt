package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DayTask
import com.example.data.model.TaskMode
import com.example.data.model.TaskPriority
import com.example.data.model.TimetableClass
import com.example.util.DateUtils

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskDialog(
    initialTask: DayTask? = null,
    defaultDate: String = DateUtils.todayDateString(),
    availableClasses: List<TimetableClass> = emptyList(),
    onSave: (DayTask) -> Unit,
    onDelete: ((Long) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var mode by remember { mutableStateOf(initialTask?.mode ?: TaskMode.STUDENT) }
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    var date by remember { mutableStateOf(initialTask?.date ?: defaultDate) }
    var time by remember { mutableStateOf(initialTask?.time ?: "") }
    var priority by remember { mutableStateOf(initialTask?.priority ?: TaskPriority.MEDIUM) }
    var category by remember {
        mutableStateOf(
            initialTask?.category ?: if (mode == TaskMode.STUDENT) "Assignment" else "Personal"
        )
    }
    var linkedCourseCode by remember { mutableStateOf(initialTask?.linkedCourseCode ?: "") }

    val studentCategories = listOf("Assignment", "Lecture Prep", "Study", "Exam Review", "Reading")
    val generalCategories = listOf("Personal", "Errand", "Health", "Fitness", "Work", "Chores")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialTask == null) "Create Task" else "Edit Task",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_add_task_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Selector: Student Mode vs General Day Task
                Text(
                    text = "Task Mode",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                SecondaryTabRow(selectedTabIndex = if (mode == TaskMode.STUDENT) 0 else 1) {
                    Tab(
                        selected = mode == TaskMode.STUDENT,
                        onClick = {
                            mode = TaskMode.STUDENT
                            if (!studentCategories.contains(category)) category = "Assignment"
                        },
                        text = { Text("🎓 Student Mode") },
                        modifier = Modifier.testTag("mode_student_tab")
                    )
                    Tab(
                        selected = mode == TaskMode.GENERAL,
                        onClick = {
                            mode = TaskMode.GENERAL
                            if (!generalCategories.contains(category)) category = "Personal"
                        },
                        text = { Text("📋 General Day Task") },
                        modifier = Modifier.testTag("mode_general_tab")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title & Description
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    placeholder = {
                        Text(
                            if (mode == TaskMode.STUDENT) "e.g. Finish Algorithm Set 3"
                            else "e.g. Buy groceries & meal prep"
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("task_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes / Details (optional)") },
                    placeholder = { Text("Add instructions or checklists...") },
                    modifier = Modifier.fillMaxWidth().testTag("task_desc_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Date & Time
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1.3f).testTag("task_date_input")
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time (optional)") },
                        placeholder = { Text("15:00") },
                        modifier = Modifier.weight(1f).testTag("task_time_input")
                    )
                }

                // If Student Mode: Course linking
                if (mode == TaskMode.STUDENT && availableClasses.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Link to Course (optional)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val distinctCourses = availableClasses.map { it.courseCode }.distinct()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        distinctCourses.take(4).forEach { c ->
                            FilterChip(
                                selected = linkedCourseCode == c,
                                onClick = {
                                    linkedCourseCode = if (linkedCourseCode == c) "" else c
                                },
                                label = { Text(c, fontSize = 11.sp) },
                                modifier = Modifier.testTag("link_course_$c")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Chips
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                val currentCategoryList = if (mode == TaskMode.STUDENT) studentCategories else generalCategories
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    currentCategoryList.take(4).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            modifier = Modifier.testTag("task_category_$cat")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Priority
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskPriority.values().forEach { prio ->
                        FilterChip(
                            selected = priority == prio,
                            onClick = { priority = prio },
                            label = { Text(prio.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            modifier = Modifier.testTag("priority_chip_${prio.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (initialTask != null && onDelete != null) {
                        Button(
                            onClick = {
                                onDelete(initialTask.id)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("delete_task_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(
                                    DayTask(
                                        id = initialTask?.id ?: 0,
                                        title = title.trim(),
                                        description = description.trim(),
                                        date = date.trim(),
                                        time = time.trim().ifBlank { null },
                                        isCompleted = initialTask?.isCompleted ?: false,
                                        priority = priority,
                                        mode = mode,
                                        linkedCourseCode = if (mode == TaskMode.STUDENT) linkedCourseCode.ifBlank { null } else null,
                                        category = category
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.weight(2f).height(48.dp).testTag("save_task_button")
                    ) {
                        Text(if (initialTask == null) "Create Task" else "Save Changes")
                    }
                }
            }
        }
    }
}
