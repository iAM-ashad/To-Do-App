package com.iamashad.foxtodo.ui.add

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar

private val sampleCategories = listOf("Personal", "Work", "Family", "Health", "Event", "Other")

fun priorityColor(priority: Int): Color = when (priority) {
    0 -> Color(0xFF2DD4BF) // teal-ish (low)
    1 -> Color(0xFF815CCC) // purple-ish (medium)
    2 -> Color(0xFFC41953) // red-ish (high)
    else -> Color(0xFF9F7AEA)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    viewModel: AddTaskViewModel,
    onSaved: (Long) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.saved.collectLatest { id -> onSaved(id) }
    }

    val datePicker = remember {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, day: Int ->
                viewModel.updateDueDate(LocalDate.of(year, month + 1, day))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    val timePicker = remember {
        TimePickerDialog(context, { _, hour, minute ->
            viewModel.updateDueTime(LocalTime.of(hour, minute))
        }, 9, 0, false)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Task") },
                navigationIcon = {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save() },
                        enabled = uiState.isValid && !uiState.isSaving
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Title
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("What is to be done?") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(12.dp))

            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Description (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(12.dp))

            // Date & Time
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { datePicker.show() }, modifier = Modifier.weight(1f)) {
                    Text(uiState.dueDate?.toString() ?: "Due Date")
                }
                OutlinedButton(onClick = { timePicker.show() }, modifier = Modifier.weight(1f)) {
                    Text(uiState.dueTime?.toString() ?: "Due Time")
                }
            }

            Spacer(Modifier.height(12.dp))

            // Reminder toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reminder/Notification", modifier = Modifier.weight(1f))
                Switch(
                    checked = uiState.reminderOn,
                    onCheckedChange = { viewModel.setReminder(it) })
            }

            Spacer(Modifier.height(12.dp))

            var expanded by remember { mutableStateOf(false) }
            val currentCategory = uiState.category ?: ""

            Box(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Note: use TextField (or OutlinedTextField) as the first child inside ExposedDropdownMenuBox
                    OutlinedTextField(
                        value = currentCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor() // important: anchor the field so the menu positions correctly
                            .fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        sampleCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    viewModel.setCategory(cat)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Priority selector
            Text("Priority", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val priorities = listOf("Low" to 0, "Medium" to 1, "High" to 2)
                priorities.forEach { (label, idx) ->
                    val selected = uiState.priority == idx
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { viewModel.setPriority(idx) }) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) priorityColor(idx) else priorityColor(idx).copy(
                                        alpha = 0.18f
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.12f))
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(label, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (uiState.error != null) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            if (uiState.isSaving) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Saving...")
                }
            }
        }
    }
}
