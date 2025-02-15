package com.example.foodapps.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.wear.compose.material.*
import androidx.wear.tooling.preview.devices.WearDevices

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val firebaseService = FirebaseService()
            WearApp(firebaseService)
        }
    }
}

@Composable
fun WearApp(firebaseService: FirebaseService) {
    val tasks = remember { mutableStateOf<List<Task>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    val listState = rememberScalingLazyListState()

    LaunchedEffect(Unit) {
        firebaseService.listenForTasks { tasks.value = it }
    }

    Scaffold(
        timeText = { TimeText() },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(tasks.value) { task ->
                TaskItem(
                    task = task,
                    onComplete = { isChecked -> firebaseService.updateTask(task.id, isChecked) },
                    onDelete = {
                        firebaseService.deleteTask(task.id) {
                            firebaseService.listenForTasks { tasks.value = it }
                        }
                    }
                )
            }
            item {
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        }
    }

    if (showDialog) {
        AddTaskDialog(
            onDismiss = { showDialog = false },
            onAddTask = { taskName ->
                firebaseService.addTask(Task(task = taskName)) {
                    firebaseService.listenForTasks { tasks.value = it }
                    showDialog = false
                }
            }
        )
    }
}

@Composable
fun TaskItem(task: Task, onComplete: (Boolean) -> Unit, onDelete: () -> Unit) {
    var isChecked by remember { mutableStateOf(task.isCompleted) }

    Card(
        onClick = {},
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        backgroundPainter = CardDefaults.cardBackgroundPainter()
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = {
                    isChecked = it
                    onComplete(it)
                },
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = task.task, style = TextStyle(fontSize = 12.sp))
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onDelete,
                modifier = Modifier.size(32.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = androidx.compose.ui.graphics.Color.Red,
                    contentColor = androidx.compose.ui.graphics.Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onAddTask: (String) -> Unit) {
    var taskText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth(0.8f) // جعل النافذة أصغر لتناسب الشاشة
                .padding(10.dp),
            backgroundPainter = CardDefaults.cardBackgroundPainter()
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Add Task", style = TextStyle(fontSize = 16.sp))
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    value = taskText,
                    onValueChange = { taskText = it },
                    textStyle = TextStyle(fontSize = 14.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            if (taskText.isNotEmpty()) {
                                onAddTask(taskText)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp(firebaseService = FirebaseService())
}
