package com.iamashad.foxtodo.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: TaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: Long = (savedStateHandle["taskId"] as? Long)
        ?: (savedStateHandle["taskId"] as? String)?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DetailEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<DetailEvent> = _events.asSharedFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val task = repository.getTask(taskId)
            if (task == null) {
                _uiState.update { it.copy(isLoading = false, error = "Task not found") }
                _events.tryEmit(DetailEvent.NotFound)
                return@launch
            }
            _uiState.update {
                it.copy(
                    task = task,
                    isLoading = false,
                    isEditing = false,
                    editTitle = task.title,
                    editDescription = task.description ?: "",
                    editDate = task.dueDateEpoch?.let { epochToLocalDate(it) },
                    editTime = task.dueDateEpoch?.let { epochToLocalTime(it) },
                    editCategory = task.category,
                    editPriority = task.priority
                )
            }
        }
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditing = !it.isEditing) }
    }

    fun setTitle(text: String) = _uiState.update { it.copy(editTitle = text, error = null) }
    fun setDescription(text: String) = _uiState.update { it.copy(editDescription = text) }
    fun setDate(date: LocalDate?) = _uiState.update { it.copy(editDate = date) }
    fun setTime(time: LocalTime?) = _uiState.update { it.copy(editTime = time) }
    fun setCategory(cat: String?) = _uiState.update { it.copy(editCategory = cat) }
    fun setPriority(pr: Int) = _uiState.update { it.copy(editPriority = pr) }
    fun toggleCompleted() {
        val current = _uiState.value.task ?: return
        viewModelScope.launch {
            repository.updateTask(current.copy(completed = !current.completed))
            load() // refresh
        }
    }

    fun save() {
        val state = _uiState.value
        if (!state.isValidForSave) {
            _uiState.update { it.copy(error = "Title cannot be empty") }
            return
        }
        val original = state.task ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val epochMillis = state.editDate?.let { d ->
                    val ldt =
                        if (state.editTime != null) d.atTime(state.editTime) else d.atStartOfDay()
                    ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }

                val updated = original.copy(
                    title = state.editTitle.trim(),
                    description = state.editDescription.ifBlank { null },
                    dueDateEpoch = epochMillis,
                    category = state.editCategory,
                    priority = state.editPriority
                )
                repository.updateTask(updated)
                _uiState.update { it.copy(task = updated, isEditing = false, isLoading = false) }
                _events.emit(DetailEvent.Saved(updated.id))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Save failed", isLoading = false) }
                _events.emit(DetailEvent.Error(e.message ?: "Save failed"))
            }
        }
    }

    fun delete(confirmed: Boolean = true) {
        if (!confirmed) return
        val current = _uiState.value.task ?: return
        viewModelScope.launch {
            try {
                repository.deleteTask(current)
                _events.emit(DetailEvent.Deleted(current.id))
            } catch (e: Exception) {
                _events.emit(DetailEvent.Error(e.message ?: "Delete failed"))
            }
        }
    }

    // helpful converters
    private fun epochToLocalDate(epoch: Long): LocalDate =
        Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).toLocalDate()

    private fun epochToLocalTime(epoch: Long): LocalTime =
        Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).toLocalTime()
}

sealed class DetailEvent {
    data class Saved(val id: Long) : DetailEvent()
    data class Deleted(val id: Long) : DetailEvent()
    data class Error(val message: String) : DetailEvent()
    object NotFound : DetailEvent()
}