package com.iamashad.foxtodo.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamashad.foxtodo.domain.model.Task
import com.iamashad.foxtodo.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    repository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState(isLoading = true))
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val allTasksFlow = repository.observeTasks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            allTasksFlow.collect { _ ->
                _uiState.update { it.copy(isLoading = false) }
            }
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    currentMonth = state.currentMonth,
                    selectedDate = state.selectedDate
                )
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun previousMonth() {
        _uiState.update { it.copy(currentMonth = it.currentMonth.minusMonths(1)) }
    }

    fun nextMonth() {
        _uiState.update { it.copy(currentMonth = it.currentMonth.plusMonths(1)) }
    }

    /**
     * Returns tasks for a particular date (local system zone).
     */
    fun tasksForDateFlow(date: LocalDate): Flow<List<Task>> {
        val zone: ZoneId = ZoneId.systemDefault()
        val startOfDayEpoch = date.atStartOfDay(zone).toInstant().toEpochMilli()
        // start of next day minus 1 ms covers the entire day range
        val endOfDayEpoch = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1L

        return allTasksFlow.map { list ->
            list.filter { task ->
                task.dueDateEpoch?.let { epoch ->
                    epoch in startOfDayEpoch..endOfDayEpoch
                } ?: false
            }.sortedBy { it.dueDateEpoch ?: Long.MAX_VALUE }
        }
    }

    /**
     * Returns a set of dates in the current month that have tasks (for drawing dots).
     */
    fun datesWithTasksInMonthFlow(yearMonth: YearMonth): Flow<Set<LocalDate>> {
        val zone: ZoneId = ZoneId.systemDefault()
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()
        return allTasksFlow.map { list ->
            list.mapNotNull { task ->
                task.dueDateEpoch?.let { epoch ->
                    val local = java.time.Instant.ofEpochMilli(epoch).atZone(zone).toLocalDate()
                    if (!local.isBefore(start) && !local.isAfter(end)) local else null
                }
            }.toSet()
        }
    }

    fun undatedTasksFlow(): Flow<List<Task>> {
        return allTasksFlow.map { list ->
            list.filter { it.dueDateEpoch == null && !it.completed }
                .sortedBy { it.id }
        }
    }
}
