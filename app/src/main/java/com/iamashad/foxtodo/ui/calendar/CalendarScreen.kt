package com.iamashad.foxtodo.ui.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamashad.foxtodo.R
import com.iamashad.foxtodo.domain.model.Task
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onOpenTask: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val month = uiState.currentMonth
    val selectedDate = uiState.selectedDate
    var showAgenda by remember { mutableStateOf(true) }

    // heavy mapping/filtering runs on Default (ViewModel)
    val datesWithTasks by viewModel.datesWithTasksInMonthFlow(month)
        .collectAsState(initial = emptySet())

    val tasksForSelected by viewModel.tasksForDateFlow(selectedDate)
        .collectAsState(initial = emptyList())

    // undated tasks page (collected directly)
    val undatedTasks by viewModel.undatedTasksFlow().collectAsState(initial = emptyList())

    // screen height cap
    val screenHeight = LocalWindowInfo.current.containerSize.height.dp

    // pager state (0 = Calendar, 1 = Undated)
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 }
    )
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Column {
                MonthHeader(
                    yearMonth = month,
                    onPrevious = { viewModel.previousMonth() },
                    onNext = { viewModel.nextMonth() },
                    onToday = { viewModel.selectDate(LocalDate.now()) }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    PageSegmentButton(
                        text = "Calendar",
                        selected = pagerState.currentPage == 0,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    PageSegmentButton(
                        text = "Undated (${undatedTasks.size})",
                        selected = pagerState.currentPage == 1,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    // slightly smaller overall pager area
                    .heightIn(max = screenHeight * 0.4f)
            ) { page ->
                if (page == 0) {
                    // --- Calendar page ---
                    Column {
                        WeekDaysRow()

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                // tighter padding around the calendar
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp) // was 8.dp
                            ) {
                                CalendarGrid(
                                    yearMonth = month,
                                    selected = selectedDate,
                                    datesWithTasks = datesWithTasks,
                                    onDateSelected = { viewModel.selectDate(it) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.size(4.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.calendar_today),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Agenda • $selectedDate",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { showAgenda = !showAgenda },
                                modifier = Modifier
                                    .padding(end = 13.dp)
                            ) {
                                Text(if (showAgenda) "Hide" else "Show")
                            }
                        }

                        AnimatedVisibility(
                            visible = showAgenda,
                            enter = fadeIn(animationSpec = tween(durationMillis = 180)),
                            exit = fadeOut(animationSpec = tween(durationMillis = 120))
                        ) {
                            if (tasksForSelected.isNotEmpty()) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    items(tasksForSelected, key = { it.id }) { task ->
                                        TaskRow(task = task, onOpen = { onOpenTask(it) })
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No tasks due for this date",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        if (undatedTasks.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No undated tasks",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(undatedTasks, key = { it.id }) { task ->
                                    TaskRow(task = task, onOpen = { onOpenTask(it) })
                                }
                            }
                        }
                    }
                }
            }

            PagerIndicator(
                currentPage = pagerState.currentPage,
                pageCount = 2,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp)
            )
        }
    }
}

/* ------------------ Helpers & cells ------------------ */

// cached formatter to avoid allocations on every compose
private val mediumDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())

@Composable
private fun PageSegmentButton(text: String, selected: Boolean, onClick: () -> Unit) {
    val bg =
        if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val fg =
        if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = text, color = fg, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PagerIndicator(currentPage: Int, pageCount: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until pageCount) {
            val active = i == currentPage
            Box(
                modifier = Modifier
                    .size(if (active) 10.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
            )
        }
    }
}

@Composable
private fun TaskRow(task: Task, onOpen: (Long) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.size(4.dp))
                val dueText = when {
                    task.completed -> "Completed"
                    task.dueDateEpoch != null -> {
                        Instant.ofEpochMilli(task.dueDateEpoch)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .format(mediumDateFormatter)
                    }

                    else -> "No deadline"
                }
                Text(text = dueText, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onOpen(task.id) }) { Text("Open") }
        }
    }
}

@Composable
private fun MonthHeader(
    yearMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrevious) {
                    Icon(
                        painter = painterResource(R.drawable.chevron_left),
                        contentDescription = "Previous",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.clickable { onToday() }) {
                    Text(
                        text = yearMonth.month.getDisplayName(
                            java.time.format.TextStyle.FULL,
                            Locale.getDefault()
                        ),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "${yearMonth.year}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(
                        painter = painterResource(R.drawable.chevron_right),
                        contentDescription = "Next",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = yearMonth.month.getDisplayName(
                        java.time.format.TextStyle.SHORT,
                        Locale.getDefault()
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun WeekDaysRow() {
    val days = arrayOf(
        DayOfWeek.SUNDAY,
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { d ->
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = d.getDisplayName(
                        java.time.format.TextStyle.SHORT,
                        Locale.getDefault()
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selected: LocalDate,
    datesWithTasks: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstOfMonth = yearMonth.atDay(1)
    val leadingEmpty = (firstOfMonth.dayOfWeek.value % 7)
    val days = remember(yearMonth) {
        val list = mutableListOf<LocalDate?>()
        val start = firstOfMonth.minusDays(leadingEmpty.toLong())
        for (i in 0 until 42) list.add(start.plusDays(i.toLong()))
        list
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(days) { day ->
            DayCell(
                date = day,
                currentMonth = yearMonth,
                isSelected = day == selected,
                hasTasks = day != null && datesWithTasks.contains(day),
                onClick = { if (day != null) onDateSelected(day) }
            )
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    currentMonth: YearMonth,
    isSelected: Boolean,
    hasTasks: Boolean,
    onClick: () -> Unit
) {
    val isCurrentMonth = date?.month == currentMonth.month

    // cheaper: static alpha, animate only scale when selection changes
    val alpha = if (isCurrentMonth) 1f else 0.35f

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.04f else 1f,
        animationSpec = tween(durationMillis = 180)
    )

    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(32.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else
                    Color.Transparent
            )
            .clickable { onClick() }
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        if (date == null) return@Box
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val today = LocalDate.now()
            val dayText = date.dayOfMonth.toString()
            val textColor = when {
                date == today -> MaterialTheme.colorScheme.primary
                isSelected -> MaterialTheme.colorScheme.onPrimary
                else -> MaterialTheme.colorScheme.onSurface
            }
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (date == today)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else
                            Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayText,
                    fontSize = 11.sp,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.size(2.dp))
            if (hasTasks) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                )
            }
        }
    }
}
