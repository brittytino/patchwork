package com.brittytino.patchwork.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brittytino.patchwork.viewmodels.IdleAppViewModel
import com.brittytino.patchwork.data.database.entity.IdleAppRule
import com.brittytino.patchwork.services.IdleAppEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdleAppScreen(
    viewModel: IdleAppViewModel = viewModel()
) {
    val rules by viewModel.rules.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()
    val showAppPicker by viewModel.showAppPicker.collectAsState()
    val showRuleEditor by viewModel.showRuleEditor.collectAsState()
    val editingRule by viewModel.editingRule.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val isMonitoring by viewModel.isMonitoring.collectAsState()
    val idleStats by viewModel.idleStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Idle App Auto-Actions") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = { viewModel.refreshStats() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAppPickerDialog() },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, "Add Rule")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                IdleInfoCard()
            }
            
            item {
                MonitoringControlCard(
                    isMonitoring = isMonitoring,
                    onToggle = { viewModel.toggleMonitoring() }
                )
            }
            
            if (rules.isEmpty()) {
                item {
                    EmptyRulesCard()
                }
            } else {
                items(rules, key = { it.id }) { rule ->
                    IdleRuleCard(
                        rule = rule,
                        stats = idleStats[rule.packageName],
                        onToggle = { viewModel.toggleRule(it) },
                        onEdit = { viewModel.editRule(it) },
                        onDelete = { viewModel.deleteRule(it) },
                        onCheckNow = { viewModel.checkAppNow(it.packageName) }
                    )
                }
            }
            
            if (recentLogs.isNotEmpty()) {
                item {
                    Text(
                        "Recent Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                items(recentLogs.take(5), key = { it.id }) { log ->
                    ActionLogCard(log)
                }
            }
        }

        // App Picker Dialog
        if (showAppPicker) {
            AppPickerDialog(
                apps = installedApps,
                onAppSelected = { app ->
                    viewModel.createRuleForApp(app)
                    viewModel.hideAppPickerDialog()
                },
                onDismiss = { viewModel.hideAppPickerDialog() }
            )
        }

        // Rule Editor Dialog
        if (showRuleEditor && editingRule != null) {
            IdleRuleEditorDialog(
                rule = editingRule!!,
                onSave = { updatedRule ->
                    viewModel.updateRule(updatedRule)
                    viewModel.hideRuleEditorDialog()
                },
                onDismiss = { viewModel.hideRuleEditorDialog() }
            )
        }
    }
}

@Composable
private fun IdleInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Autorenew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    "Automated Background Cleanup",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Automatically freeze, kill, or notify about apps that haven't been used. Reclaim RAM and battery life.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun MonitoringControlCard(
    isMonitoring: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isMonitoring) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (isMonitoring) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    tint = if (isMonitoring) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column {
                    Text(
                        "Background Monitoring",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (isMonitoring) "Checking idle apps every 5 minutes" else "Not monitoring",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Switch(
                checked = isMonitoring,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
private fun IdleRuleCard(
    rule: IdleAppRule,
    stats: IdleAppEngine.IdleStats?,
    onToggle: (IdleAppRule) -> Unit,
    onEdit: (IdleAppRule) -> Unit,
    onDelete: (IdleAppRule) -> Unit,
    onCheckNow: (IdleAppRule) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (rule.enabled) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        rule.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            getActionIcon(rule.action),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "${rule.action.name} after ${rule.idleThresholdMinutes}m",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { onCheckNow(rule) }) {
                        Icon(Icons.Default.PlayArrow, "Check Now", tint = MaterialTheme.colorScheme.tertiary)
                    }
                    IconButton(onClick = { onEdit(rule) }) {
                        Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                    Switch(
                        checked = rule.enabled,
                        onCheckedChange = { onToggle(rule) }
                    )
                }
            }
            
            if (stats != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IdleStatItem(
                        label = "Idle Time",
                        value = formatIdleTime(stats.idleMinutes),
                        icon = Icons.Default.Timer,
                        isWarning = stats.idleMinutes >= rule.idleThresholdMinutes
                    )
                    IdleStatItem(
                        label = "Actions Taken",
                        value = "${rule.actionCount}x",
                        icon = Icons.Default.Done
                    )
                }
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Idle Rule?") },
            text = { Text("Remove automated actions for ${rule.appName}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(rule)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun IdleStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isWarning: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isWarning) MaterialTheme.colorScheme.error else Color.Unspecified
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionLogCard(log: com.brittytino.patchwork.data.database.entity.IdleAppActionLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                getActionIcon(log.action),
                contentDescription = null,
                tint = if (log.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    log.appName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${log.action.name} • Idle ${log.idleTimeMinutes}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!log.success && log.errorMessage != null) {
                    Text(
                        log.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Text(
                formatTime(log.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AppPickerDialog(
    apps: List<IdleAppViewModel.AppInfo>,
    onAppSelected: (IdleAppViewModel.AppInfo) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredApps = remember(apps, searchQuery) {
        if (searchQuery.isBlank()) {
            apps
        } else {
            apps.filter {
                it.appName.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select App for Auto-Actions") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search apps...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, null)
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAppSelected(app) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = app.icon.toBitmap(48, 48).asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            
                            Text(
                                app.appName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IdleRuleEditorDialog(
    rule: IdleAppRule,
    onSave: (IdleAppRule) -> Unit,
    onDismiss: () -> Unit
) {
    var editedRule by remember { mutableStateOf(rule) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Idle Rule: ${rule.appName}") },
        text = {
            LazyColumn(
                modifier = Modifier.height(400.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Idle Threshold
                item {
                    Column {
                        Text(
                            "Idle Threshold",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Slider(
                            value = editedRule.idleThresholdMinutes.toFloat(),
                            onValueChange = {
                                editedRule = editedRule.copy(idleThresholdMinutes = it.toInt())
                            },
                            valueRange = 30f..720f,
                            steps = 22
                        )
                        Text(
                            "${editedRule.idleThresholdMinutes} minutes (${formatIdleTime(editedRule.idleThresholdMinutes.toLong())})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                item { Divider() }
                
                // Action Selection
                item {
                    Text(
                        "Action to Take",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    ActionOption(
                        action = IdleAppRule.Action.NOTIFY,
                        selected = editedRule.action == IdleAppRule.Action.NOTIFY,
                        onClick = { editedRule = editedRule.copy(action = IdleAppRule.Action.NOTIFY) },
                        description = "Show notification only"
                    )
                }
                
                item {
                    ActionOption(
                        action = IdleAppRule.Action.KILL,
                        selected = editedRule.action == IdleAppRule.Action.KILL,
                        onClick = { editedRule = editedRule.copy(action = IdleAppRule.Action.KILL) },
                        description = "Kill background processes"
                    )
                }
                
                item {
                    ActionOption(
                        action = IdleAppRule.Action.FREEZE,
                        selected = editedRule.action == IdleAppRule.Action.FREEZE,
                        onClick = { editedRule = editedRule.copy(action = IdleAppRule.Action.FREEZE) },
                        description = "Freeze app completely (requires root)"
                    )
                }
                
                item {
                    ActionOption(
                        action = IdleAppRule.Action.CLEAR_CACHE,
                        selected = editedRule.action == IdleAppRule.Action.CLEAR_CACHE,
                        onClick = { editedRule = editedRule.copy(action = IdleAppRule.Action.CLEAR_CACHE) },
                        description = "Clear app cache (requires root)"
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(editedRule) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ActionOption(
    action: IdleAppRule.Action,
    selected: Boolean,
    onClick: () -> Unit,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick
            )
            Icon(
                getActionIcon(action),
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Column {
                Text(
                    action.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyRulesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Autorenew,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "No Idle App Rules",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Add apps to automatically manage when idle",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getActionIcon(action: IdleAppRule.Action): androidx.compose.ui.graphics.vector.ImageVector {
    return when (action) {
        IdleAppRule.Action.FREEZE -> Icons.Default.AcUnit
        IdleAppRule.Action.KILL -> Icons.Default.Close
        IdleAppRule.Action.CLEAR_CACHE -> Icons.Default.CleaningServices
        IdleAppRule.Action.NOTIFY -> Icons.Default.Notifications
    }
}

private fun formatIdleTime(minutes: Long): String {
    val hours = minutes / 60
    val days = hours / 24
    return when {
        days > 0 -> "${days}d ${hours % 24}h"
        hours > 0 -> "${hours}h ${minutes % 60}m"
        else -> "${minutes}m"
    }
}

private fun formatTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / 60000
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        days > 0 -> "${days}d ago"
        hours > 0 -> "${hours}h ago"
        minutes > 0 -> "${minutes}m ago"
        else -> "Just now"
    }
}
