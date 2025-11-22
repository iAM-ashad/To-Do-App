package com.iamashad.foxtodo.ui.profile

data class ProfileUiState(
    val username: String = "Ashad",
    val avatarInitials: String = "A",
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val tasksByCategory: Map<String, Int> = emptyMap(),
    val recentCounts: List<Int> = emptyList(),
    val isDarkMode: Boolean = false,
    val isLoading: Boolean = true,
    val lastExportPath: String? = null,
    val error: String? = null
)
