package com.iamashad.foxtodo

import com.iamashad.foxtodo.domain.repository.TaskRepository
import com.iamashad.foxtodo.ui.add.AddTaskViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddTaskViewModelTest {

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    private lateinit var repo: TaskRepository
    private lateinit var vm: AddTaskViewModel

    @Before
    fun setup() {
        repo = mockk()
        vm = AddTaskViewModel(repo)
    }

    @Test
    fun `save emits saved id and calls repository`() = coroutineRule.runTest {
        // arrange
        val title = "Buy milk"
        vm.updateTitle(title)
        coEvery { repo.addTask(any()) } returns 123L

        // act
        vm.save()

        coroutineRule.scope.advanceUntilIdle()

        coVerify { repo.addTask(match { it.title == title }) }

        MutableSharedFlow<Long>(replay = 1)
        var got = -1L
        val job = coroutineRule.scope.launch {
            vm.saved.collect { id -> got = id }
        }

        vm.save()
        coroutineRule.scope.advanceUntilIdle()

        assertEquals(123L, got)
        job.cancel()
    }

    @Test
    fun `save without title sets error`() = coroutineRule.runTest {
        // arrangement: title empty by default
        vm.save()
        coroutineRule.scope.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isValid)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Please enter", true))
    }
}
