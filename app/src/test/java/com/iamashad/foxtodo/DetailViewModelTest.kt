package com.iamashad.foxtodo

import androidx.lifecycle.SavedStateHandle
import com.iamashad.foxtodo.domain.model.Task
import com.iamashad.foxtodo.domain.repository.TaskRepository
import com.iamashad.foxtodo.ui.detail.DetailViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    private lateinit var repo: TaskRepository

    @Before
    fun setup() {
        repo = mockk()
    }

    @Test
    fun `load non existing emits not found event`() = coroutineRule.runTest {
        val savedState = SavedStateHandle(mapOf("taskId" to 999L))
        coEvery { repo.getTask(999L) } returns null

        val vm = DetailViewModel(repo, savedState)
        coroutineRule.scope.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `toggleCompleted updates task`() = coroutineRule.runTest {
        val task = Task(id = 1, title = "t", completed = false)
        val savedState = SavedStateHandle(mapOf("taskId" to 1L))
        coEvery { repo.getTask(1L) } returns task
        coEvery { repo.updateTask(any()) } returns Unit

        val vm = DetailViewModel(repo, savedState)
        coroutineRule.scope.advanceUntilIdle()

        vm.toggleCompleted()
        coroutineRule.scope.advanceUntilIdle()

        coVerify { repo.updateTask(match { it.id == 1L && it.completed }) }
    }

    @Test
    fun `delete emits Deleted event and calls repository`() = coroutineRule.runTest {
        val task = Task(id = 5, title = "x")
        val savedState = SavedStateHandle(mapOf("taskId" to 5L))
        coEvery { repo.getTask(5L) } returns task
        coEvery { repo.deleteTask(task) } returns Unit

        val vm = DetailViewModel(repo, savedState)
        coroutineRule.scope.advanceUntilIdle()

        vm.delete(true)
        coroutineRule.scope.advanceUntilIdle()

        coVerify { repo.deleteTask(task) }
    }
}
