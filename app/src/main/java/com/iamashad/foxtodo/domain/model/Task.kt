package com.iamashad.foxtodo.domain.model


data class Task(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val dueDateEpoch: Long? = null,
    val completed: Boolean = false,
    val category: String? = null,
    val priority: Int = 1
)
