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
import com.brittytino.patchwork.viewmodels.AppCooldownViewModel
import com.brittytino.patchwork.services.AppCooldownEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCooldownScreen(
    viewModel: AppCooldownViewModel = viewModel()
) {
    val rules by viewModel.rules.collectAsState()
    val showAppPicker by viewModel.showAppPicker.collectAsState()
    val showRuleEditor by viewModel.showRuleEditor.collectAsState()
    val editingRule by viewModel.editingRule.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val appStats by viewModel.appStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart App Cooldown") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = { viewModel.refreshStats() }) {
                        Icon(Icons.Default.Refresh, "Refresh Stats")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAppPickerDialog() },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, "Add Cooldown Rule")
            }
        }
    ) { padding ->
        if (rules.isEmpty()) {
            EmptyCooldownState(
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    CooldownInfoCard()
                }
                
                items(rules, key = { it.id }) { rule ->
                    CooldownRuleCard(
                        rule = rule,
                        stats = appStats[rule.packageName],
                        onToggle = { viewModel.toggleRule(it) },
                        onEdit = { viewModel.editRule(it) },
                        onDelete = { viewModel.deleteRule(it) }
                    )
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
            CooldownRuleEditorDialog(
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
private fun CooldownInfoCard() {
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
                Icons.Default.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    "Break the Doom-Scroll Cycle",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Set cooldown periods to prevent compulsive app reopening. Enforce healthy usage limits and take control of your screen time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun CooldownRuleCard(
    rule: com.brittytino.patchwork.data.database.entity.AppCooldownRule,
    stats: AppCooldownEngine.AppStats?,
    onToggle: (com.brittytino.patchwork.data.database.entity.AppCooldownRule) -> Unit,
    onEdit: (com.brittytino.patchwork.data.database.entity.AppCooldownRule) -> Unit,
    onDelete: (com.brittytino.patchwork.data.database.entity.AppCooldownRule) -> Unit
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
                    Text(
                        "${rule.cooldownPeriodMinutes} minute cooldown",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Rule Settings Summary
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (rule.maxDailyOpens != null) {
                    RuleSummaryItem(
                        icon = Icons.Default.CalendarToday,
                        text = "Max ${rule.maxDailyOpens} opens per day",
                        value = stats?.let { "${it.todayOpens}/${rule.maxDailyOpens} today" }
                    )
                }
                if (rule.maxHourlyOpens != null) {
                    RuleSummaryItem(
                        icon = Icons.Default.Schedule,
                        text = "Max ${rule.maxHourlyOpens} opens per hour",
                        value = stats?.let { "${it.hourlyOpens}/${rule.maxHourlyOpens} this hour" }
                    )
                }
                if (rule.blockLaunch) {
                    RuleSummaryItem(
                        icon = Icons.Default.Block,
                        text = "Launch blocked during cooldown"
                    )
                } else if (rule.showWarningDialog) {
                    RuleSummaryItem(
                        icon = Icons.Default.Warning,
                        text = "Warning shown during cooldown"
                    )
                }
            }
            
            // Usage Statistics
            if (stats != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Today",
                        value = "${stats.todayOpens} opens",
                        icon = Icons.Default.TouchApp
                    )
                    StatItem(
                        label = "Screen Time",
                        value = formatDuration(stats.totalScreenTimeMs),
                        icon = Icons.Default.PhoneAndroid
                    )
                }
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Cooldown Rule?") },
            text = { Text("Remove cooldown protection for ${rule.appName}?") },
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
private fun RuleSummaryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    value: String? = null
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AppPickerDialog(
    apps: List<AppCooldownViewModel.AppInfo>,
    onAppSelected: (AppCooldownViewModel.AppInfo) -> Unit,
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
        title = { Text("Select App for Cooldown") },
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
private fun CooldownRuleEditorDialog(
    rule: com.brittytino.patchwork.data.database.entity.AppCooldownRule,
    onSave: (com.brittytino.patchwork.data.database.entity.AppCooldownRule) -> Unit,
    onDismiss: () -> Unit
) {
    var editedRule by remember { mutableStateOf(rule) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Cooldown: ${rule.appName}") },
        text = {
            LazyColumn(
                modifier = Modifier.height(450.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cooldown Period
                item {
                    Column {
                        Text(
                            "Cooldown Period",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Slider(
                            value = editedRule.cooldownPeriodMinutes.toFloat(),
                            onValueChange = {
                                editedRule = editedRule.copy(cooldownPeriodMinutes = it.toInt())
                            },
                            valueRange = 5f..120f,
                            steps = 22
                        )
                        Text(
                            "${editedRule.cooldownPeriodMinutes} minutes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Minimum time between app reopens",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                item { Divider() }
                
                // Usage Limits
                item {
                    Text(
                        "Usage Limits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    LimitSetting(
                        label = "Max Opens Per Day",
                        value = editedRule.maxDailyOpens,
                        onValueChange = {
                            editedRule = editedRule.copy(maxDailyOpens = it)
                        }
                    )
                }
                
                item {
                    LimitSetting(
                        label = "Max Opens Per Hour",
                        value = editedRule.maxHourlyOpens,
                        onValueChange = {
                            editedRule = editedRule.copy(maxHourlyOpens = it)
                        }
                    )
                }
                
                item { Divider() }
                
                // Blocking Behavior
                item {
                    Text(
                        "Blocking Behavior",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Block App Launch", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Prevent app from opening during cooldown",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = editedRule.blockLaunch,
                            onCheckedChange = {
                                editedRule = editedRule.copy(blockLaunch = it)
                            }
                        )
                    }
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Show Warning", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Display cooldown reminder dialog",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = editedRule.showWarningDialog,
                            onCheckedChange = {
                                editedRule = editedRule.copy(showWarningDialog = it)
                            }
                        )
                    }
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
private fun LimitSetting(
    label: String,
    value: Int?,
    onValueChange: (Int?) -> Unit
) {
    var enabled by remember(value) { mutableStateOf(value != null) }
    var sliderValue by remember(value) { mutableStateOf((value ?: 10).toFloat()) }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = enabled,
                onCheckedChange = {
                    enabled = it
                    onValueChange(if (it) sliderValue.toInt() else null)
                }
            )
        }
        
        if (enabled) {
            Spacer(modifier = Modifier.height(4.dp))
            Slider(
                value = sliderValue,
                onValueChange = {
                    sliderValue = it
                    onValueChange(it.toInt())
                },
                valueRange = 1f..50f,
                steps = 48
            )
            Text(
                "${sliderValue.toInt()} opens",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyCooldownState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Timer,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    CircleShape
                )
                .padding(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "No Cooldown Rules",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Add cooldown rules to break the cycle of compulsive app checking and build healthier screen time habits",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

private fun formatDuration(ms: Long): String {
    val minutes = ms / 60000
    val hours = minutes / 60
    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m"
        else -> "<1m"
    }
}
