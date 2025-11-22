package com.iamashad.foxtodo.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iamashad.foxtodo.domain.model.Task
import com.iamashad.foxtodo.ui.add.priorityColor
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TaskCard(
    modifier: Modifier = Modifier,
    task: Task,
    showCheckbox: Boolean = true,   // <-- NEW
    onToggleComplete: (Task) -> Unit,
    onOpen: (Task) -> Unit
) {
    val accent = priorityColor(task.priority)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpen(task) },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = accent.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left color strip
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(accent, shape = MaterialTheme.shapes.small)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    if (showCheckbox) {
                        Checkbox(
                            checked = task.completed,
                            onCheckedChange = { onToggleComplete(task) }
                        )
                    }

                    if (showCheckbox) Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = task.category ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = task.category ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (task.completed) "Completed"
                    else (task.dueDateEpoch?.let { formatTimeShort(it) } ?: ""),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun formatTimeShort(epochMillis: Long): String {
    val instant = Instant.ofEpochMilli(epochMillis)
    val zdt = instant.atZone(ZoneId.systemDefault())
    val fmt = DateTimeFormatter.ofPattern("h:mm a")
    return zdt.format(fmt)
}
