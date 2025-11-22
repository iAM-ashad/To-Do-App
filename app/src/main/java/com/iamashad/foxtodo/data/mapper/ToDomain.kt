package com.iamashad.foxtodo.data.mapper

import com.iamashad.foxtodo.data.local.TaskEntity
import com.iamashad.foxtodo.domain.model.Task

fun TaskEntity.toDomain() = Task(
    id = this.id,
    title = this.title,
    description = this.description,
    dueDateEpoch = this.dueDateEpoch,
    completed = this.completed,
    category = this.category,
    priority = this.priority
)
