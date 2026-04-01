package com.example.todolist.ui
/*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todolist.vm.TaskViewModel
import androidx.compose.ui.graphics.Color
@Composable
fun MainScreen(vm: TaskViewModel) {

    val tasks by vm.tasks.collectAsState(initial = emptyList())

    var showAdd by remember { mutableStateOf(false) }
    Box(modifier=Modifier.fillMaxSize())
    {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAdd = true },
                    containerColor = Color(0xFFF9F6F0),
                    contentColor = Color(0xFF2C2C2C)
                ){
                    Text("+")
                }
            }
        ) { pad ->

            LazyColumn(
                modifier = Modifier
                    .padding(pad)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tasks.size) { i ->
                    TaskCard(tasks[i], vm)
                }
            }

            if (showAdd) {
                AddTaskDialog(
                    onDismiss = { showAdd = false },
                    onAdd = { title, dur, alarm ->
                        vm.addTask(title, dur, alarm)
                        showAdd = false
                    }
                )
            }
        }
    }
}


 */

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.todolist.vm.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


val ParchmentBase = Color(0xFFF9F6F0)
val ParchmentEdge = Color(0xFFEBE3D5)
val InkDark = Color(0xFF2C2C2C)
//val ButtonGray = Color(0xFF9E9E9E)

@Composable
fun MainScreen(vm: TaskViewModel) {

    val tasks by vm.tasks.collectAsState(initial = emptyList())

    var showAdd by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            // --- UPDATED: Extended Floating Action Button ---
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showAdd = true },
                    // Using professional Parchment and Ink color scheme
                    containerColor = ParchmentEdge, // Slightly darker than the background for contrast
                    contentColor = InkDark,
                    shape = RoundedCornerShape(16.dp), // Matching  card rounded corners
                    icon = { Icon(Icons.Rounded.Add, contentDescription = "Add Task") },
                    text = {
                        Text(
                            text = "New Task",
                            fontWeight = FontWeight.ExtraBold, // Makes it pop
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
            }
        ) { pad ->

            LazyColumn(
                modifier = Modifier
                    .padding(pad)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- NEW: Header Section ---
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp) // Extra spacing before the first card
                    ) {
                        // Main Title
                        Text(
                            text = "Today's Tasks",
                            color = InkDark,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.ExtraBold
                            )
                        )

                        // Dynamic Current Date Subtitle
                        val currentDate = remember {
                            SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
                        }
                        Text(
                            text = currentDate,
                            color = ButtonGray,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // --- EXISTING: List Items ---
                // (Note: Optimized to use the 'items' lambda for lists)
                items(tasks) { task ->
                    TaskCard(task, vm)
                }
            }

            if (showAdd) {
                AddTaskDialog(
                    onDismiss = { showAdd = false },
                    onAdd = { title, dur, alarm ->
                        vm.addTask(title, dur, alarm)
                        showAdd = false
                    }
                )
            }
        }
    }
}