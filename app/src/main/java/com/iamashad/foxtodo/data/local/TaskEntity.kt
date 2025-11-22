package com.iamashad.foxtodo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String? = null,
    val dueDateEpoch: Long? = null,
    val completed: Boolean = false,
    val category: String? = null,
    val priority: Int = 1
)
