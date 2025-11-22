package com.iamashad.foxtodo.ui.home

import com.iamashad.foxtodo.domain.model.Task
import java.time.LocalDate

data class HomeUiState(
    val allTasks: List<Task> = emptyList(),
    val temporarilyRemoved: List<Task> = emptyList(),
    val filter: TaskFilter = TaskFilter.ALL,
    val selectedDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = true,
    val snackbarMessage: String? = null
)

enum class TaskFilter { ALL, COMPLETED, PENDING }
