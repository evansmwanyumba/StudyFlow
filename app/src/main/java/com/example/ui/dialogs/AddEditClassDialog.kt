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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.TimetableClass
import com.example.ui.components.DurationInput
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClassDialog(
    initialClass: TimetableClass? = null,
    defaultDayOfWeek: Int = 1,
    defaultRole: String = "STUDENT",
    onSave: (TimetableClass) -> Unit,
    onDelete: ((Long) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var role by remember { mutableStateOf(initialClass?.role ?: defaultRole) }
    var unitCode by remember { mutableStateOf(initialClass?.courseCode ?: "") }
    var unitTitle by remember { mutableStateOf(initialClass?.courseName ?: "") }
    var instructor by remember { mutableStateOf(initialClass?.instructor ?: "") }
    var venue by remember { mutableStateOf(initialClass?.location ?: "") }
    var dayOfWeek by remember { mutableIntStateOf(initialClass?.dayOfWeek ?: defaultDayOfWeek) }
    var startTime by remember { mutableStateOf(initialClass?.startTime ?: "09:00") }
    var endTime by remember { mutableStateOf(initialClass?.endTime ?: "10:30") }
    var category by remember { mutableStateOf(initialClass?.category ?: "Lecture") }

    // User manual reminder time with choices of seconds, minutes, and/or hours
    var customReminderSeconds by remember {
        mutableIntStateOf(initialClass?.totalReminderSeconds(1800) ?: 1800) // Default 30 min (1800s)
    }
    var isMorningEnabled by remember { mutableStateOf(initialClass?.isMorningBriefingEnabled ?: true) }
    var isPreSessionEnabled by remember { mutableStateOf(initialClass?.isPreSessionReminderEnabled ?: true) }
    var notifyOnSessionStart by remember { mutableStateOf(initialClass?.notifyOnSessionStart ?: true) }

    val categories = listOf("Lecture", "Lab", "Tutorial", "Seminar", "Workshop")

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
                    Column {
                        Text(
                            text = if (initialClass == null) "Add Timetable Unit" else "Edit Timetable Unit",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Input unit, venue, time, and custom reminders",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_add_class_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Role Selector: Student vs Teacher/Lecturer
                SecondaryTabRow(selectedTabIndex = if (role == "STUDENT") 0 else 1) {
                    Tab(
                        selected = role == "STUDENT",
                        onClick = { role = "STUDENT" },
                        text = { Text("🎓 Student Timetable") },
                        modifier = Modifier.testTag("role_student_tab")
                    )
                    Tab(
                        selected = role == "TEACHER",
                        onClick = { role = "TEACHER" },
                        text = { Text("👨‍🏫 Lecturer Timetable") },
                        modifier = Modifier.testTag("role_teacher_tab")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Day of Week Picker
                Text(
                    text = "Day of the Week",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (d in 1..7) {
                        val isSelected = dayOfWeek == d
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { dayOfWeek = d }
                                .testTag("day_picker_$d"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = DateUtils.shortDayName(d).take(1),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Unit Code & Title
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = unitCode,
                        onValueChange = { unitCode = it },
                        label = { Text("Unit Code") },
                        placeholder = { Text("e.g. CS101") },
                        modifier = Modifier.weight(1f).testTag("course_code_input")
                    )
                    OutlinedTextField(
                        value = unitTitle,
                        onValueChange = { unitTitle = it },
                        label = { Text("Unit Title / Name") },
                        placeholder = { Text("Data Structures") },
                        modifier = Modifier.weight(1.8f).testTag("course_name_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Time Pickers (Start - End)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        placeholder = { Text("09:00") },
                        modifier = Modifier.weight(1f).testTag("start_time_input")
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        placeholder = { Text("10:30") },
                        modifier = Modifier.weight(1f).testTag("end_time_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Venue & Lecturer / Class Group
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = venue,
                        onValueChange = { venue = it },
                        label = { Text("Venue / Hall") },
                        placeholder = { Text("e.g. Room 304") },
                        modifier = Modifier.weight(1f).testTag("location_input")
                    )
                    OutlinedTextField(
                        value = instructor,
                        onValueChange = { instructor = it },
                        label = { Text(if (role == "TEACHER") "Class Group" else "Lecturer") },
                        placeholder = { Text(if (role == "TEACHER") "Year 2 CS" else "Dr. Smith") },
                        modifier = Modifier.weight(1f).testTag("instructor_input")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Chips
                Text(
                    text = "Session Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            modifier = Modifier.testTag("category_chip_$cat")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Manual Duration Input with Seconds, Minutes, or Hours
                DurationInput(
                    initialTotalSeconds = customReminderSeconds,
                    label = "Advance Reminder Time (Seconds / Minutes / Hours):",
                    onDurationChanged = { customReminderSeconds = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Alert Toggles Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // 1. Session Starting / Started Alert
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Session Starting NOW Alert", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Alerts at exact start time: 'Session starting NOW at venue'", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = notifyOnSessionStart,
                                onCheckedChange = { notifyOnSessionStart = it },
                                modifier = Modifier.testTag("notify_session_start_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 2. Pre-Session Advance Alert
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Advance Pre-Session Alert", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Notifies beforehand based on your custom time above", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isPreSessionEnabled,
                                onCheckedChange = { isPreSessionEnabled = it },
                                modifier = Modifier.testTag("pre_session_alert_switch")
                            )
                        }

                        if (role == "STUDENT") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Morning Wake-Up Reminder", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("Reminder to wake up and get ready for your morning class", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = isMorningEnabled,
                                    onCheckedChange = { isMorningEnabled = it },
                                    modifier = Modifier.testTag("morning_alert_switch")
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (initialClass != null && onDelete != null) {
                        Button(
                            onClick = {
                                onDelete(initialClass.id)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("delete_class_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                    Button(
                        onClick = {
                            if (unitCode.isNotBlank()) {
                                onSave(
                                    TimetableClass(
                                        id = initialClass?.id ?: 0,
                                        courseCode = unitCode.trim().uppercase(),
                                        courseName = unitTitle.trim().ifBlank { unitCode.trim() },
                                        instructor = instructor.trim(),
                                        location = venue.trim(),
                                        dayOfWeek = dayOfWeek,
                                        startTime = startTime.trim(),
                                        endTime = endTime.trim(),
                                        colorHex = if (role == "TEACHER") "#0D9488" else "#2563EB",
                                        category = category,
                                        role = role,
                                        customReminderSeconds = customReminderSeconds,
                                        customReminderMinutes = customReminderSeconds / 60,
                                        isMorningBriefingEnabled = if (role == "STUDENT") isMorningEnabled else false,
                                        isPreSessionReminderEnabled = isPreSessionEnabled,
                                        notifyOnSessionStart = notifyOnSessionStart
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = unitCode.isNotBlank(),
                        modifier = Modifier.weight(2f).height(48.dp).testTag("save_class_button")
                    ) {
                        Text(if (initialClass == null) "Add Unit" else "Save Changes")
                    }
                }
            }
        }
    }
}
