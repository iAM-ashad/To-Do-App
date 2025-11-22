package com.iamashad.foxtodo

import com.iamashad.foxtodo.domain.model.Task
import com.iamashad.foxtodo.domain.repository.TaskRepository
import com.iamashad.foxtodo.ui.calendar.CalendarViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    private lateinit var repo: TaskRepository

    @Before
    fun setup() {
        repo = mockk()
    }

    @Test
    fun `tasksForDateFlow returns tasks on that date`() = coroutineRule.runTest {
        val zone = ZoneId.systemDefault()
        val date = LocalDate.of(2025, 11, 15)
        val epoch = date.atStartOfDay(zone).toInstant().toEpochMilli()

        val tasks = listOf(
            Task(id = 1, title = "A", dueDateEpoch = epoch),
            Task(id = 2, title = "B", dueDateEpoch = epoch + 5_000)
        )

        val flow = MutableStateFlow(tasks)
        every { repo.observeTasks() } returns flow

        val vm = CalendarViewModel(repo)
        coroutineRule.scope.advanceUntilIdle()

        val result = vm.tasksForDateFlow(date)
        val collected = result.first()

        assertEquals(2, collected.size)
    }

    @Test
    fun `datesWithTasksInMonthFlow returns set`() = coroutineRule.runTest {
        val date = LocalDate.of(2025, 11, 15)
        val zone = ZoneId.systemDefault()
        val epoch = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val tasks = listOf(Task(id = 1, title = "A", dueDateEpoch = epoch))
        val flow = MutableStateFlow(tasks)
        every { repo.observeTasks() } returns flow

        val vm = CalendarViewModel(repo)
        coroutineRule.scope.advanceUntilIdle()

        val ym = java.time.YearMonth.of(2025, 11)
        val set = vm.datesWithTasksInMonthFlow(ym).first()

        assertTrue(set.contains(date))
    }
}
