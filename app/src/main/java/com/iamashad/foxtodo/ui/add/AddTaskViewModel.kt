package com.iamashad.foxtodo.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iamashad.foxtodo.domain.model.Task
import com.iamashad.foxtodo.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class AddTaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTaskUiState())
    val uiState: StateFlow<AddTaskUiState> = _uiState.asStateFlow()

    // Emitted once when save succeeds, so UI can navigate back
    private val _saved = MutableSharedFlow<Long>(replay = 0)
    val saved: SharedFlow<Long> = _saved.asSharedFlow()

    // Update helpers
    fun updateTitle(text: String) = _uiState.update { it.copy(title = text, error = null) }
    fun updateDescription(text: String) = _uiState.update { it.copy(description = text) }
    fun updateDueDate(date: LocalDate?) = _uiState.update { it.copy(dueDate = date) }
    fun updateDueTime(time: LocalTime?) = _uiState.update { it.copy(dueTime = time) }
    fun setReminder(on: Boolean) = _uiState.update { it.copy(reminderOn = on) }
    fun setCategory(cat: String?) = _uiState.update { it.copy(category = cat) }
    fun setPriority(priority: Int) = _uiState.update { it.copy(priority = priority) }

    fun save() {
        val state = _uiState.value
        if (!state.isValid) {
            _uiState.update { it.copy(error = "Please enter a title") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val epochMillis = state.dueDate?.let { d ->
                    // combine date + optional time if provided
                    val localDateTime = if (state.dueTime != null) {
                        d.atTime(state.dueTime)
                    } else {
                        d.atStartOfDay()
                    }
                    localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }

                val task = Task(
                    id = 0L,
                    title = state.title.trim(),
                    description = state.description.ifBlank { null },
                    dueDateEpoch = epochMillis,
                    completed = false,
                    category = state.category,
                    priority = state.priority
                )
                val newId = repository.addTask(task)
                _saved.emit(newId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Save failed") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}