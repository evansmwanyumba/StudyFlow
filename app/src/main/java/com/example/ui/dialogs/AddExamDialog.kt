package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Exam
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExamDialog(
    onSave: (Exam) -> Unit,
    onDismiss: () -> Unit
) {
    var unitCode by remember { mutableStateOf("") }
    var unitTitle by remember { mutableStateOf("") }
    var examDate by remember { mutableStateOf(DateUtils.todayDateString()) }
    var examTime by remember { mutableStateOf("09:00") }
    var venue by remember { mutableStateOf("") }
    var reminderFrequency by remember { mutableStateOf("Daily") }
    var studyNotes by remember { mutableStateOf("") }

    val frequencies = listOf("Daily", "Every 2 Days", "Weekly", "1 Day Before")

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Add Upcoming Exam",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Priority 1 • Study reminders will be scheduled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_add_exam_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Unit Code & Title
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = unitCode,
                        onValueChange = { unitCode = it },
                        label = { Text("Unit Code") },
                        placeholder = { Text("CS201") },
                        modifier = Modifier.weight(1f).testTag("exam_unit_code_input")
                    )
                    OutlinedTextField(
                        value = unitTitle,
                        onValueChange = { unitTitle = it },
                        label = { Text("Unit Title") },
                        placeholder = { Text("Algorithms Final") },
                        modifier = Modifier.weight(1.8f).testTag("exam_unit_title_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Date & Time
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = examDate,
                        onValueChange = { examDate = it },
                        label = { Text("Exam Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1.4f).testTag("exam_date_input")
                    )
                    OutlinedTextField(
                        value = examTime,
                        onValueChange = { examTime = it },
                        label = { Text("Time (optional)") },
                        placeholder = { Text("09:00") },
                        modifier = Modifier.weight(1f).testTag("exam_time_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Venue
                OutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = { Text("Exam Venue / Hall") },
                    placeholder = { Text("e.g. Auditorium Hall A") },
                    modifier = Modifier.fillMaxWidth().testTag("exam_venue_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Reminder Frequency
                Text(
                    text = "Study Reminder Frequency",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    frequencies.forEach { freq ->
                        FilterChip(
                            selected = reminderFrequency == freq,
                            onClick = { reminderFrequency = freq },
                            label = { Text(freq, fontSize = 11.sp) },
                            modifier = Modifier.testTag("freq_$freq")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Study Notes
                OutlinedTextField(
                    value = studyNotes,
                    onValueChange = { studyNotes = it },
                    label = { Text("Syllabus / Topics to Study") },
                    placeholder = { Text("e.g. Modules 1-4, Graph Algorithms, Past Papers") },
                    modifier = Modifier.fillMaxWidth().height(90.dp).testTag("exam_notes_input")
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (unitCode.isNotBlank()) {
                            onSave(
                                Exam(
                                    unitCode = unitCode.trim().uppercase(),
                                    unitTitle = unitTitle.trim().ifBlank { unitCode.trim() },
                                    examDate = examDate.trim(),
                                    examTime = examTime.trim(),
                                    venue = venue.trim(),
                                    reminderFrequency = reminderFrequency,
                                    studyNotes = studyNotes.trim()
                                )
                            )
                            onDismiss()
                        }
                    },
                    enabled = unitCode.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_exam_btn")
                ) {
                    Text("Save Exam & Schedule Reminders")
                }
            }
        }
    }
}
