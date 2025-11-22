package com.iamashad.foxtodo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamashad.foxtodo.domain.model.Task
import com.iamashad.foxtodo.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        repository.observeTasks()
            .onEach { list ->
                _uiState.update { it.copy(allTasks = list, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    val visibleTasks: Flow<List<Task>> = uiState.map { state ->
        state.allTasks.filter { task ->
            val matchesDate = state.selectedDate.let { sel ->
                task.dueDateEpoch?.let { epoch ->
                    val local = Instant.ofEpochMilli(epoch)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    local == sel
                } ?: true
            }

            val matchesFilter = when (state.filter) {
                TaskFilter.ALL -> true
                TaskFilter.COMPLETED -> task.completed
                TaskFilter.PENDING -> !task.completed
            }

            matchesDate && matchesFilter
        }
    }

    fun toggleComplete(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(completed = !task.completed)
            try {
                repository.updateTask(updated)
            } catch (e: Exception) {
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun setFilter(filter: TaskFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun progressPercent(tasksForUi: List<Task>): Int {
        val total = tasksForUi.size
        if (total == 0) return 0
        val completed = tasksForUi.count { it.completed }
        return ((completed * 100) / total)
    }
}
