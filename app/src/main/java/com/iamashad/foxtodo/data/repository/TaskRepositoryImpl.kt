package com.iamashad.foxtodo.data.repository

import com.iamashad.foxtodo.data.local.TaskDao
import com.iamashad.foxtodo.data.mapper.toDomain
import com.iamashad.foxtodo.data.mapper.toEntity
import com.iamashad.foxtodo.domain.model.Task
import com.iamashad.foxtodo.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(private val dao: TaskDao) : TaskRepository {

    override fun observeTasks(): Flow<List<Task>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getTask(id: Long): Task? =
        dao.getById(id)?.toDomain()

    override suspend fun addTask(task: Task): Long =
        dao.insert(task.toEntity())

    override suspend fun updateTask(task: Task) =
        dao.update(task.toEntity())

    override suspend fun deleteTask(task: Task) =
        dao.delete(task.toEntity())
}

