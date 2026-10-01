package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class DurationUnit(val label: String, val multiplierSeconds: Int) {
    SECONDS("Seconds", 1),
    MINUTES("Minutes", 60),
    HOURS("Hours", 3600)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurationInput(
    initialTotalSeconds: Int,
    label: String,
    onDurationChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine best initial unit
    val (initUnit, initVal) = when {
        initialTotalSeconds % 3600 == 0 && initialTotalSeconds >= 3600 ->
            Pair(DurationUnit.HOURS, initialTotalSeconds / 3600)
        initialTotalSeconds % 60 == 0 && initialTotalSeconds >= 60 ->
            Pair(DurationUnit.MINUTES, initialTotalSeconds / 60)
        else ->
            Pair(DurationUnit.SECONDS, initialTotalSeconds)
    }

    var textValue by remember(initialTotalSeconds) { mutableStateOf(initVal.toString()) }
    var selectedUnit by remember(initialTotalSeconds) { mutableStateOf(initUnit) }

    fun emitChange(numStr: String, unit: DurationUnit) {
        val num = numStr.toIntOrNull() ?: 0
        val totalSec = (num * unit.multiplierSeconds).coerceAtLeast(1)
        onDurationChanged(totalSec)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = textValue,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }
                    textValue = filtered
                    emitChange(filtered, selectedUnit)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .width(90.dp)
                    .testTag("duration_value_input")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DurationUnit.values().forEach { unit ->
                    FilterChip(
                        selected = selectedUnit == unit,
                        onClick = {
                            selectedUnit = unit
                            emitChange(textValue, unit)
                        },
                        label = { Text(unit.label, fontSize = 11.sp) },
                        modifier = Modifier.testTag("unit_chip_${unit.name.lowercase()}")
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        val totalCalculated = (textValue.toIntOrNull() ?: 0) * selectedUnit.multiplierSeconds
        Text(
            text = "Total advance notice: $totalCalculated seconds (${formatSecondsToHuman(totalCalculated)})",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

fun formatSecondsToHuman(seconds: Int): String {
    val hrs = seconds / 3600
    val rem = seconds % 3600
    val mins = rem / 60
    val secs = rem % 60
    val parts = mutableListOf<String>()
    if (hrs > 0) parts.add("$hrs hr${if (hrs > 1) "s" else ""}")
    if (mins > 0) parts.add("$mins min${if (mins > 1) "s" else ""}")
    if (secs > 0 || parts.isEmpty()) parts.add("$secs sec${if (secs > 1) "s" else ""}")
    return parts.joinToString(" ")
}
