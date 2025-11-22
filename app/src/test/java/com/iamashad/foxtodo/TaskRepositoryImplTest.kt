package com.iamashad.foxtodo

import com.iamashad.foxtodo.data.local.TaskDao
import com.iamashad.foxtodo.data.local.TaskEntity
import com.iamashad.foxtodo.data.repository.TaskRepositoryImpl
import com.iamashad.foxtodo.domain.model.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskRepositoryImplTest {

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    private class FakeDao : TaskDao {
        private val list = mutableListOf<TaskEntity>()
        private val flow = MutableStateFlow<List<TaskEntity>>(emptyList())

        override fun observeAll() = flow

        override suspend fun getById(id: Long) = list.find { it.id == id }

        override suspend fun insert(entity: TaskEntity): Long {
            val id = if (entity.id == 0L) (list.size + 1).toLong() else entity.id
            val toInsert = entity.copy(id = id)
            list.add(toInsert)
            flow.value = list.toList()
            return id
        }

        override suspend fun update(entity: TaskEntity) {
            val idx = list.indexOfFirst { it.id == entity.id }
            if (idx >= 0) list[idx] = entity
            flow.value = list.toList()
        }

        override suspend fun delete(entity: TaskEntity) {
            list.removeAll { it.id == entity.id }
            flow.value = list.toList()
        }
    }

    @Test
    fun `insert and observe map correctly`() = coroutineRule.runTest {
        val dao = FakeDao()
        val repo = TaskRepositoryImpl(dao)
        val id = repo.addTask(Task(title = "abc"))
        assertEquals(1L, id)

        val observed = repo.observeTasks().first()
        assertEquals(1, observed.size)
        assertEquals("abc", observed.first().title)
    }
}
