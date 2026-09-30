package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.TimetableClass
import com.example.util.DateUtils

@Composable
fun AddEditClassDialog(
    initialClass: TimetableClass? = null,
    defaultDayOfWeek: Int = 1,
    onSave: (TimetableClass) -> Unit,
    onDelete: ((Long) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var courseCode by remember { mutableStateOf(initialClass?.courseCode ?: "") }
    var courseName by remember { mutableStateOf(initialClass?.courseName ?: "") }
    var instructor by remember { mutableStateOf(initialClass?.instructor ?: "") }
    var location by remember { mutableStateOf(initialClass?.location ?: "") }
    var dayOfWeek by remember { mutableIntStateOf(initialClass?.dayOfWeek ?: defaultDayOfWeek) }
    var startTime by remember { mutableStateOf(initialClass?.startTime ?: "09:00") }
    var endTime by remember { mutableStateOf(initialClass?.endTime ?: "10:30") }
    var category by remember { mutableStateOf(initialClass?.category ?: "Lecture") }
    var colorHex by remember { mutableStateOf(initialClass?.colorHex ?: "#2563EB") }
    var isMorningEnabled by remember { mutableStateOf(initialClass?.isMorningBriefingEnabled ?: true) }
    var isPreSessionEnabled by remember { mutableStateOf(initialClass?.isPreSessionReminderEnabled ?: true) }

    val categories = listOf("Lecture", "Lab", "Tutorial", "Seminar", "Exam")
    val colorOptions = listOf(
        "#2563EB", "#0D9488", "#8B5CF6", "#D97706",
        "#EC4899", "#10B981", "#06B6D4", "#6366F1"
    )

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
                        text = if (initialClass == null) "Add Class Session" else "Edit Class",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_add_class_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Day of Week Picker
                Text(
                    text = "Day of Week",
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

                // Course Code & Name
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = courseCode,
                        onValueChange = { courseCode = it },
                        label = { Text("Code") },
                        placeholder = { Text("CS101") },
                        modifier = Modifier.weight(1f).testTag("course_code_input")
                    )
                    OutlinedTextField(
                        value = courseName,
                        onValueChange = { courseName = it },
                        label = { Text("Course Name") },
                        placeholder = { Text("Data Structures") },
                        modifier = Modifier.weight(2f).testTag("course_name_input")
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

                // Instructor & Location
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = instructor,
                        onValueChange = { instructor = it },
                        label = { Text("Instructor (optional)") },
                        placeholder = { Text("Dr. Smith") },
                        modifier = Modifier.weight(1f).testTag("instructor_input")
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Room / Location") },
                        placeholder = { Text("Hall 304") },
                        modifier = Modifier.weight(1f).testTag("location_input")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Chips
                Text(
                    text = "Category",
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

                Spacer(modifier = Modifier.height(12.dp))

                // Color accent
                Text(
                    text = "Card Color",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.forEach { hex ->
                        val isSelected = colorHex == hex
                        val parsedColor = try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (_: Exception) {
                            MaterialTheme.colorScheme.primary
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { colorHex = hex }
                                .testTag("color_option_$hex")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Reminders Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Morning 2h Alert", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Notify 2 hours before morning classes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isMorningEnabled,
                                onCheckedChange = { isMorningEnabled = it },
                                modifier = Modifier.testTag("morning_alert_switch")
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("30m Prior Session Alert", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Notify 30 mins before this class begins", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isPreSessionEnabled,
                                onCheckedChange = { isPreSessionEnabled = it },
                                modifier = Modifier.testTag("pre_session_alert_switch")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
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
                            if (courseCode.isNotBlank()) {
                                onSave(
                                    TimetableClass(
                                        id = initialClass?.id ?: 0,
                                        courseCode = courseCode.trim().uppercase(),
                                        courseName = courseName.trim().ifBlank { courseCode.trim() },
                                        instructor = instructor.trim(),
                                        location = location.trim(),
                                        dayOfWeek = dayOfWeek,
                                        startTime = startTime.trim(),
                                        endTime = endTime.trim(),
                                        colorHex = colorHex,
                                        category = category,
                                        isMorningBriefingEnabled = isMorningEnabled,
                                        isPreSessionReminderEnabled = isPreSessionEnabled
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = courseCode.isNotBlank(),
                        modifier = Modifier.weight(2f).height(48.dp).testTag("save_class_button")
                    ) {
                        Text(if (initialClass == null) "Add Class" else "Save Changes")
                    }
                }
            }
        }
    }
}
