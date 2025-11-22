package com.iamashad.foxtodo.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.iamashad.foxtodo.R
import com.iamashad.foxtodo.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    themeViewModel: ThemeViewModel,
    onSignOut: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeState by themeViewModel.themeState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var notificationsOn by remember { mutableStateOf(false) }

    val percent = if (uiState.totalTasks == 0)
        0
    else
        (uiState.completedTasks * 100 / uiState.totalTasks)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- 1) User overview row ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        uiState.avatarInitials,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(uiState.username, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ashad.ansari@example.com",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"Staying Productive...\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Edit profile not implemented",
                            duration = SnackbarDuration.Short
                        )
                    }
                }) {
                    Icon(
                        painter = painterResource(R.drawable.edit),
                        contentDescription = "Edit Profile",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // --- 2) Analytics card ---
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Task Completed this Week", style = MaterialTheme.typography.bodySmall)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "${uiState.completedTasks}/${uiState.totalTasks}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("$percent%", style = MaterialTheme.typography.titleMedium)
                    }

                    LinearProgressIndicator(
                        progress = (percent.coerceIn(0, 100) / 100f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Category used most", style = MaterialTheme.typography.bodySmall)
                    val topCat = uiState.tasksByCategory.maxByOrNull { it.value }?.key ?: "None"
                    Text(
                        topCat,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // --- 3) Settings list ---
            HorizontalDivider()

            // Dark mode (ThemeViewModel)
            ListItem(
                headlineContent = { Text("Dark Mode") },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.switch_theme),
                        contentDescription = "Switch Theme Toggle",
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingContent = {
                    Switch(
                        checked = themeState.isDarkMode,
                        onCheckedChange = { checked ->
                            themeViewModel.setDarkMode(checked)
                        }
                    )
                },
                modifier = Modifier
                    .clickable {
                        themeViewModel.setDarkMode(!themeState.isDarkMode)
                    }
            )

            // Dynamic color (ThemeViewModel)
            ListItem(
                headlineContent = { Text("Dynamic Theme") },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.palette),
                        contentDescription = "Dynamic Theme Toggle",
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingContent = {
                    Switch(
                        checked = themeState.useDynamicColor,
                        onCheckedChange = { new ->
                            themeViewModel.setDynamicColor(new)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Dynamic theme: ${if (new) "ON" else "OFF"}",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )
                },
                modifier = Modifier.clickable {
                    val new = !themeState.useDynamicColor
                    themeViewModel.setDynamicColor(new)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Dynamic theme: ${if (new) "ON" else "OFF"}",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )

            // Notifications (still UI-only)
            ListItem(
                headlineContent = { Text("Notifications") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null
                    )
                },
                trailingContent = {
                    Switch(
                        checked = notificationsOn,
                        onCheckedChange = { new ->
                            notificationsOn = new
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Notifications ${if (new) "enabled" else "disabled"} (UI only)",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )
                },
                modifier = Modifier.clickable {
                    notificationsOn = !notificationsOn
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Notifications setting toggled (UI only)",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )

            // Sign out
            ListItem(
                headlineContent = { Text("Sign out") },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.log_out),
                        contentDescription = "Sign out",
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.clickable {
                    onSignOut()
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Signed out (placeholder)",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )
        }
    }
}
