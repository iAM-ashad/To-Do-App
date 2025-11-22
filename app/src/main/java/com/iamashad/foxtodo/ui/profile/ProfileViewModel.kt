package com.iamashad.foxtodo.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamashad.foxtodo.domain.model.Task
import com.iamashad.foxtodo.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    repository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val allTasks =
        repository.observeTasks().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            allTasks.collect { list ->
                _uiState.update { prev ->
                    val total = list.size
                    val completed = list.count { it.completed }
                    val byCategory = list.groupingBy { it.category ?: "Uncategorized" }.eachCount()
                    val recent = computeRecentCounts(list, days = 7)
                    prev.copy(
                        totalTasks = total,
                        completedTasks = completed,
                        tasksByCategory = byCategory,
                        recentCounts = recent,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun computeRecentCounts(tasks: List<Task>, days: Int): List<Int> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        return (0 until days).map { i ->
            val d = today.minusDays((days - 1 - i).toLong())
            tasks.count { t ->
                t.dueDateEpoch?.let { epoch ->
                    val local = Instant.ofEpochMilli(epoch).atZone(zone).toLocalDate()
                    local == d
                } ?: false
            }
        }
    }
}
