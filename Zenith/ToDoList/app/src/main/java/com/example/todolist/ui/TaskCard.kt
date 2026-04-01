package com.example.todolist.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.todolist.data.TaskEntity
import com.example.todolist.data.TaskState
import com.example.todolist.vm.TaskViewModel

val CardBg = Color(0xFFF9F6F0)
val ButtonRed = Color(0xFFC27A62)
val ButtonGray = Color(0xFF9E9E9E)
val ProgressBlueFill = Color(0xFF6B87A1)
val ProgressTrack = Color(0xFFE2E2E2)
val ChipBg = Color(0xFFEBE3D5)
val TextDark = Color(0xFF2C2C2C)

@Composable
fun TaskCard(task: TaskEntity, vm: TaskViewModel) {
    val progress = if (task.durationSec != null && task.durationSec > 0) {
        (task.elapsedSec / task.durationSec.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row with Title and Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = task.title,
                    color = TextDark,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { vm.delete(task) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete Task",
                        tint = ButtonGray
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Info Chips Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                task.durationSec?.let {
                    InfoChip(
                        icon = Icons.Rounded.Schedule,
                        text = com.example.todolist.ui.util.formatDurationSec(it)
                    )
                }

                task.alarmTime?.let {
                    InfoChip(
                        icon = Icons.Rounded.Alarm,
                        text = com.example.todolist.ui.util.formatToAmPm(it)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (task.durationSec != null) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = TextDark,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Control Buttons Logic
            if (task.durationSec != null) {
                // UI for Timer Tasks
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = ProgressBlueFill,
                    trackColor = ProgressTrack,
                    strokeCap = StrokeCap.Round
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (task.state != TaskState.RUNNING && task.state != TaskState.DONE) {
                        ActionButton(
                            text = "Start",
                            color = ButtonRed,
                            modifier = Modifier.weight(1f),
                            onClick = { vm.start(task) }
                        )
                    }

                    if (task.state == TaskState.RUNNING) {
                        ActionButton(
                            text = "Pause",
                            icon = Icons.Rounded.Pause,
                            color = ButtonRed,
                            modifier = Modifier.weight(1f),
                            onClick = { vm.pause(task) }
                        )
                    }

                    if (task.state != TaskState.DONE) {
                        ActionButton(
                            text = "Done",
                            icon = Icons.Rounded.Check,
                            color = ButtonGray,
                            modifier = Modifier.weight(1f),
                            onClick = { vm.done(task) }
                        )
                    }
                }
            } else {
                // UI for Basic Tasks (No Timer)
                if (task.state != TaskState.DONE) {
                    ActionButton(
                        text = "Done",
                        icon = Icons.Rounded.Check,
                        color = ButtonGray,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { vm.done(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun InfoChip(icon: ImageVector, text: String) {
    Surface(
        color = ChipBg,
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = TextDark, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, color = TextDark, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector? = null,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        modifier = modifier
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}