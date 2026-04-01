package com.example.todolist.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.todolist.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, durationSec: Int?, alarmTimeMillis: Long?) -> Unit
) {

    val context = LocalContext.current

    var title by remember { mutableStateOf("") }

    var timerEnabled by remember { mutableStateOf(false) }
    var hours by remember { mutableStateOf(0) }
    var minutes by remember { mutableStateOf(25) }
    var seconds by remember { mutableStateOf(0) }

    var alarmEnabled by remember { mutableStateOf(false) }
    var alarmTimeMillis by remember { mutableStateOf<Long?>(null) }

    val timeFormatter = remember {
        SimpleDateFormat("hh:mm a", Locale.getDefault())
    }

    fun openTimePicker() {
        val cal = Calendar.getInstance()

        TimePickerDialog(
            context,
            { _, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                alarmTimeMillis = cal.timeInMillis
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()
    }

    // Change your Dialog declaration to this:
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Removes default narrow constraints
    ) {

        Surface(
            shape = RoundedCornerShape(26.dp),
            tonalElevation = 6.dp,
            color = ParchmentBase,
            modifier = Modifier
                .fillMaxWidth(0.95f) // Expands the card to 95% of the screen width
                .padding(16.dp)
        ) {


            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(ParchmentBase, ParchmentEdge)
                        )
                    )
            ) {

                Column(
                    modifier = Modifier
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add Task",
                            style = AppTypography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close")
                        }
                    }

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Task name") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Timer toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Schedule, null, tint = InkSoft)
                        Spacer(Modifier.width(8.dp))
                        Text("Timer", modifier = Modifier.weight(1f))
                        Switch(
                            checked = timerEnabled,
                            onCheckedChange = { timerEnabled = it }
                        )
                    }

                    if (timerEnabled) {
                        DurationRow(
                            hours = hours,
                            minutes = minutes,
                            seconds = seconds,
                            onHoursChange = { hours = it },
                            onMinutesChange = { minutes = it },
                            onSecondsChange = { seconds = it }
                        )
                    }

                    // Alarm toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Alarm, null, tint = InkSoft)
                        Spacer(Modifier.width(8.dp))
                        Text("Alarm", modifier = Modifier.weight(1f))
                        Switch(
                            checked = alarmEnabled,
                            onCheckedChange = { alarmEnabled = it }
                        )
                    }

                    if (alarmEnabled) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openTimePicker() }
                                .padding(vertical = 6.dp)
                        ) {
                            Icon(Icons.Rounded.Alarm, null, tint = InkSoft)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = alarmTimeMillis?.let {
                                    timeFormatter.format(Date(it))
                                } ?: "Pick time",
                                color = if (alarmTimeMillis == null) InkSoft else InkDark
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Buttons
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val totalSec = if (timerEnabled) {
                                    (hours * 3600) + (minutes * 60) + seconds
                                } else null

                                val alarm = if (alarmEnabled) alarmTimeMillis else null

                                onAdd(title.trim(), totalSec, alarm)
                            },
                            enabled = title.isNotBlank() && (!timerEnabled || (hours > 0 || minutes > 0 || seconds > 0)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Rounded.Check, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
}



@Composable
private fun DurationRow(
    hours: Int,
    minutes: Int,
    seconds: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Rounded.Schedule, null, tint = InkSoft)
        Spacer(Modifier.width(8.dp))

        // We pass Modifier.weight(1f) to each field so they share space equally
        TimeInputField(modifier = Modifier.weight(1f), value = hours, label = "h", onChange = onHoursChange, max = 99)
        Text(":", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 4.dp))
        TimeInputField(modifier = Modifier.weight(1f), value = minutes, label = "m", onChange = onMinutesChange, max = 59)
        Text(":", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 4.dp))
        TimeInputField(modifier = Modifier.weight(1f), value = seconds, label = "s", onChange = onSecondsChange, max = 59)
    }
}

@Composable
private fun TimeInputField(
    modifier: Modifier = Modifier, // Added modifier parameter
    value: Int,
    label: String,
    onChange: (Int) -> Unit,
    max: Int
) {
    OutlinedTextField(
        value = if (value == 0) "" else value.toString(),
        onValueChange = {
            val v = it.toIntOrNull() ?: 0
            if (v in 0..max) onChange(v)
        },
        placeholder = { Text("00") },
        suffix = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier, // Uses the weight modifier instead of hardcoded width
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center,
            fontSize = 16.sp // Forces the font to a safe size
        )
    )
}