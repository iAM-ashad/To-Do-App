package com.iamashad.foxtodo.ui.detail

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iamashad.foxtodo.ui.add.priorityColor
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit = {},
    onDeleted: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }

    /** One-off events **/
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is DetailEvent.Saved -> {}
                is DetailEvent.Deleted -> onDeleted(event.id)
                is DetailEvent.Error -> {}
                is DetailEvent.NotFound -> onBack()
            }
        }
    }

    /** Date & Time pickers **/
    val datePicker = remember {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _: DatePicker, year, month, day ->
                viewModel.setDate(LocalDate.of(year, month + 1, day))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    val timePicker = remember {
        TimePickerDialog(
            context,
            { _, hour, minute -> viewModel.setTime(LocalTime.of(hour, minute)) },
            9, 0, false
        )
    }

    BackHandler {
        if (uiState.isEditing) viewModel.toggleEditMode() else onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isEditing) "Edit task" else "Task details",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.isEditing) viewModel.toggleEditMode()
                        else onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState.isEditing) {
                        TextButton(
                            onClick = { viewModel.save() },
                            enabled = uiState.isValidForSave
                        ) {
                            Text("Save")
                        }
                    } else {
                        TextButton(onClick = { viewModel.toggleEditMode() }) {
                            Text("Edit")
                        }
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val task = uiState.task ?: return@Scaffold

        val accent = priorityColor(task.priority)

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            /** HEADER CARD **/
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.extraLarge,
                color = accent.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    /** Left colored strip **/
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(96.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(accent)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    /** LEFT CONTENT **/
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        /** Title **/
                        Text(
                            text = if (uiState.isEditing) uiState.editTitle else task.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        /** Chips **/
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusChip(task.completed)
                            PriorityChip(priority = task.priority)
                        }

                        /** Meta row **/
                        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                            MetaLabel(
                                "When",
                                task.dueDateEpoch?.let { epochToReadable(it) } ?: "No date"
                            )
                            MetaLabel(
                                "Category",
                                task.category ?: "No category"
                            )
                        }
                    }
                }
            }

            /** VIEW MODE **/
            if (!uiState.isEditing) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 1.dp,
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Notes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            task.description ?: "No additional notes",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                /** EDIT MODE **/
                OutlinedTextField(
                    value = uiState.editTitle,
                    onValueChange = { viewModel.setTitle(it) },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.editDescription,
                    onValueChange = { viewModel.setDescription(it) },
                    label = { Text("Description / notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { datePicker.show() }
                    ) { Text(uiState.editDate?.toString() ?: "Pick date") }

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { timePicker.show() }
                    ) { Text(uiState.editTime?.toString() ?: "Pick time") }
                }

                OutlinedTextField(
                    value = uiState.editCategory ?: "",
                    onValueChange = { viewModel.setCategory(it) },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )

                /** Priority segmented **/
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PriorityButton("Low", 0, uiState.editPriority) {
                        viewModel.setPriority(0)
                    }
                    PriorityButton("Medium", 1, uiState.editPriority) {
                        viewModel.setPriority(1)
                    }
                    PriorityButton("High", 2, uiState.editPriority) {
                        viewModel.setPriority(2)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete task?") },
            text = { Text("Are you sure? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.delete(true)
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatusChip(completed: Boolean) {
    val label = if (completed) "Completed" else "Pending"
    val color = if (completed) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.primaryContainer

    AssistChip(
        onClick = {},
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (completed) MaterialTheme.colorScheme.primaryContainer.copy(0.9f) else MaterialTheme.colorScheme.primary.copy(0.9f),
            labelColor = color
        )
    )
}

@Composable
private fun PriorityChip(priority: Int) {
    val label = priorityLabel(priority)
    val color = priorityColor(priority)

    AssistChip(
        onClick = {},
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.16f),
            labelColor = color
        )
    )
}

@Composable
private fun MetaLabel(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PriorityButton(
    label: String,
    priority: Int,
    selectedPriority: Int,
    onClick: () -> Unit
) {
    val selected = priority == selectedPriority

    Button(
        onClick = onClick,
        modifier = Modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected)
                priorityColor(priority)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun epochToReadable(epoch: Long): String {
    val instant = java.time.Instant.ofEpochMilli(epoch)
    val zdt = instant.atZone(java.time.ZoneId.systemDefault())
    val date = zdt.toLocalDate()
    val time = zdt.toLocalTime().withSecond(0).withNano(0)
    return "$date • $time"
}

private fun priorityLabel(priority: Int) = when (priority) {
    0 -> "Low"
    1 -> "Medium"
    2 -> "High"
    else -> "Medium"
}
