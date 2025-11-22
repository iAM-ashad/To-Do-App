package com.iamashad.foxtodo

import com.iamashad.foxtodo.domain.model.Task
import com.iamashad.foxtodo.domain.repository.TaskRepository
import com.iamashad.foxtodo.ui.home.HomeViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    @Test
    fun `progress percent calculates correctly`() = coroutineRule.runTest {
        val tasksForUi = listOf(
            Task(id = 1, title = "a", completed = true),
            Task(id = 2, title = "b", completed = false),
            Task(id = 3, title = "c", completed = true)
        )
        val vm = HomeViewModel(mockk(relaxed = true))
        val percent = vm.progressPercent(tasksForUi)
        // 2 completed out of 3 -> 66 (integer)
        assertEquals(66, percent)
    }

    @Test
    fun `visibleTasks filters by date and filter`() = coroutineRule.runTest {
        val repo = mockk<TaskRepository>()
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val epochToday = today.atStartOfDay(zone).toInstant().toEpochMilli()

        val list = listOf(
            Task(1, "t1", dueDateEpoch = epochToday, completed = false),
            Task(2, "t2", dueDateEpoch = epochToday, completed = true),
            Task(3, "t3", dueDateEpoch = null, completed = false)
        )
        every { repo.observeTasks() } returns MutableStateFlow(list)

        val vm = HomeViewModel(repo)
        // give the VM a chance to collect the repo flow
        coroutineRule.scope.advanceUntilIdle()

        // collect the first snapshot from the visibleTasks Flow
        val first = vm.visibleTasks.first()

        assertEquals(3, first.size)
    }
}
