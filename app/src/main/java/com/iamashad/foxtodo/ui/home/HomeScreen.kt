package com.iamashad.foxtodo.ui.home

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material3.Icon as M3Icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenTask: (Long) -> Unit = {},
    onAddTask: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val tasks by viewModel.visibleTasks.collectAsState(initial = emptyList())

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val fmt = DateTimeFormatter.ofPattern("EEEE, d MMM", Locale.getDefault())
                    Text(text = uiState.selectedDate.format(fmt))
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddTask, text = { Text("Add") }, icon = {
                M3Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            ProgressInsightCard(
                percent = viewModel.progressPercent(tasks),
                modifier = Modifier.padding(16.dp)
            )

            FilterChips(selected = uiState.filter, onSelected = { viewModel.setFilter(it) })

            if (tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tasks for this date", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(tasks, key = { it.id }) { task ->

                        val dismissState = rememberSwipeToDismissBoxState(
                            positionalThreshold = { distance -> distance * 0.20f }
                        )

                        // Observe the per-item dismiss state. We WILL NOT remove the item from UI
                        // here — we await the snackbar result and then call deleteTask if needed.
                        LaunchedEffect(dismissState) {
                            snapshotFlow { dismissState.currentValue }.collect { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    // Show snackbar from this item coroutine — item remains in UI until we decide.
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Task deleted",
                                        actionLabel = "UNDO",
                                        duration = SnackbarDuration.Long
                                    )

                                    if (result == SnackbarResult.ActionPerformed) {
                                        // user tapped UNDO — do nothing (optionally animate reset)
                                    } else {
                                        // user didn't undo — persist the deletion
                                        viewModel.deleteTask(task)
                                    }

                                    // Reset swipe visuals
                                    dismissState.reset()
                                }
                            }
                        }

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clip(MaterialTheme.shapes.medium)
                                            .background(Color(0xFFEF9A9A))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Deleting",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        ) {
                            TaskCard(
                                task = task,
                                onToggleComplete = { viewModel.toggleComplete(it) },
                                onOpen = { onOpenTask(it.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressInsightCard(percent: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Today's Task Insight", style = MaterialTheme.typography.bodyLarge)
                Text("$percent%", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = percent / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
        }
    }
}

@Composable
fun FilterChips(selected: TaskFilter, onSelected: (TaskFilter) -> Unit) {
    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selected == TaskFilter.ALL,
            onClick = { onSelected(TaskFilter.ALL) },
            label = { Text("All") })
        FilterChip(
            selected = selected == TaskFilter.COMPLETED,
            onClick = { onSelected(TaskFilter.COMPLETED) },
            label = { Text("Completed") })
        FilterChip(
            selected = selected == TaskFilter.PENDING,
            onClick = { onSelected(TaskFilter.PENDING) },
            label = { Text("Pending") })
    }
}

