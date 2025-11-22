package com.iamashad.foxtodo.ui.add

import java.time.LocalDate
import java.time.LocalTime

data class AddTaskUiState(
    val title: String = "",
    val description: String = "",
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val reminderOn: Boolean = false,
    val category: String? = null,
    val priority: Int = 1, // 0=low,1=med,2=high
    val isSaving: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean
        get() = title.isNotBlank()
}