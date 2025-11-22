package com.iamashad.foxtodo.ui.detail

import com.iamashad.foxtodo.domain.model.Task
import java.time.LocalDate
import java.time.LocalTime

data class DetailUiState(
    val task: Task? = null,
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val editTitle: String = "",
    val editDescription: String = "",
    val editDate: LocalDate? = null,
    val editTime: LocalTime? = null,
    val editCategory: String? = null,
    val editPriority: Int = 1,
    val error: String? = null
) {
    val isValidForSave: Boolean
        get() = editTitle.isNotBlank()
}