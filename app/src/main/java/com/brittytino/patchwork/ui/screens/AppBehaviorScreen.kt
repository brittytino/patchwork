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
import com.brittytino.patchwork.viewmodels.AppBehaviorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBehaviorScreen(
    viewModel: AppBehaviorViewModel = viewModel()
) {
    val rules by viewModel.rules.collectAsState()
    val showAppPicker by viewModel.showAppPicker.collectAsState()
    val selectedRule by viewModel.selectedRule.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Behavior Controller") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAppPicker() },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, "Add Rule")
            }
        }
    ) { padding ->
        if (rules.isEmpty()) {
            EmptyRulesState(
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
                    BehaviorInfoCard()
                }
                
                items(rules, key = { it.id }) { rule ->
                    RuleCard(
                        rule = rule,
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
                    viewModel.hideAppPicker()
                },
                onDismiss = { viewModel.hideAppPicker() }
            )
        }

        // Rule Editor Dialog
        if (selectedRule != null) {
            RuleEditorDialog(
                rule = selectedRule!!,
                onSave = { updatedRule ->
                    viewModel.updateRule(updatedRule)
                },
                onDismiss = { viewModel.closeEditor() }
            )
        }
    }
}

@Composable
private fun BehaviorInfoCard() {
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
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    "Per-App Behavior Rules",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Automatically adjust volume, brightness, privacy settings, and more when specific apps are in the foreground",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun RuleCard(
    rule: com.brittytino.patchwork.data.database.entity.AppBehaviorRule,
    onToggle: (com.brittytino.patchwork.data.database.entity.AppBehaviorRule) -> Unit,
    onEdit: (com.brittytino.patchwork.data.database.entity.AppBehaviorRule) -> Unit,
    onDelete: (com.brittytino.patchwork.data.database.entity.AppBehaviorRule) -> Unit
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        rule.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
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
            
            // Rule Summary
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                rule.setMediaVolume?.let {
                    RuleSummaryItem(
                        icon = Icons.Default.VolumeUp,
                        text = "Media Volume: $it%"
                    )
                }
                rule.setRingVolume?.let {
                    RuleSummaryItem(
                        icon = Icons.Default.PhoneInTalk,
                        text = "Ring Volume: $it%"
                    )
                }
                rule.setBrightness?.let {
                    RuleSummaryItem(
                        icon = Icons.Default.LightMode,
                        text = "Brightness: $it%"
                    )
                }
                if (rule.keepScreenAwake) {
                    RuleSummaryItem(
                        icon = Icons.Default.WbTwilight,
                        text = "Keep Screen Awake"
                    )
                }
                if (rule.disableScreenshots) {
                    RuleSummaryItem(
                        icon = Icons.Default.Block,
                        text = "Disable Screenshots"
                    )
                }
                if (rule.clearClipboardOnExit) {
                    RuleSummaryItem(
                        icon = Icons.Default.ContentCut,
                        text = "Clear Clipboard on Exit"
                    )
                }
                if (rule.blockNotifications) {
                    RuleSummaryItem(
                        icon = Icons.Default.NotificationsOff,
                        text = "Block Notifications"
                    )
                }
            }
            
            // Usage Stats
            if (rule.applyCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Applied ${rule.applyCount} times",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Rule?") },
            text = { Text("Remove behavior rules for ${rule.appName}?") },
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
    text: String
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
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AppPickerDialog(
    apps: List<com.brittytino.patchwork.viewmodels.AppBehaviorViewModel.AppInfo>,
    onAppSelected: (com.brittytino.patchwork.viewmodels.AppBehaviorViewModel.AppInfo) -> Unit,
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
        title = { Text("Select App") },
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
                            app.icon?.let { drawable ->
                                Image(
                                    bitmap = drawable.toBitmap(48, 48).asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                            
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
private fun RuleEditorDialog(
    rule: com.brittytino.patchwork.data.database.entity.AppBehaviorRule,
    onSave: (com.brittytino.patchwork.data.database.entity.AppBehaviorRule) -> Unit,
    onDismiss: () -> Unit
) {
    var editedRule by remember { mutableStateOf(rule) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${rule.appName}") },
        text = {
            LazyColumn(
                modifier = Modifier.height(500.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Audio Section
                item {
                    Text(
                        "Audio Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    SliderSetting(
                        label = "Media Volume",
                        value = editedRule.setMediaVolume?.toFloat() ?: -1f,
                        onValueChange = {
                            editedRule = editedRule.copy(
                                setMediaVolume = if (it < 0) null else it.toInt()
                            )
                        },
                        enabled = editedRule.setMediaVolume != null,
                        onEnabledChange = {
                            editedRule = editedRule.copy(
                                setMediaVolume = if (it) 50 else null
                            )
                        }
                    )
                }
                
                item {
                    SliderSetting(
                        label = "Ring Volume",
                        value = editedRule.setRingVolume?.toFloat() ?: -1f,
                        onValueChange = {
                            editedRule = editedRule.copy(
                                setRingVolume = if (it < 0) null else it.toInt()
                            )
                        },
                        enabled = editedRule.setRingVolume != null,
                        onEnabledChange = {
                            editedRule = editedRule.copy(
                                setRingVolume = if (it) 50 else null
                            )
                        }
                    )
                }
                
                // Display Section
                item {
                    Text(
                        "Display Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    SliderSetting(
                        label = "Brightness",
                        value = editedRule.setBrightness?.toFloat() ?: -1f,
                        onValueChange = {
                            editedRule = editedRule.copy(
                                setBrightness = if (it < 0) null else it.toInt()
                            )
                        },
                        enabled = editedRule.setBrightness != null,
                        onEnabledChange = {
                            editedRule = editedRule.copy(
                                setBrightness = if (it) 50 else null
                            )
                        }
                    )
                }
                
                item {
                    SwitchSetting(
                        label = "Keep Screen Awake",
                        checked = editedRule.keepScreenAwake,
                        onCheckedChange = {
                            editedRule = editedRule.copy(keepScreenAwake = it)
                        }
                    )
                }
                
                item {
                    SwitchSetting(
                        label = "Night Light",
                        checked = editedRule.enableNightLight ?: false,
                        onCheckedChange = {
                            editedRule = editedRule.copy(
                                enableNightLight = if (it) true else null
                            )
                        }
                    )
                }
                
                // Privacy Section
                item {
                    Text(
                        "Privacy Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    SwitchSetting(
                        label = "Disable Screenshots",
                        checked = editedRule.disableScreenshots,
                        onCheckedChange = {
                            editedRule = editedRule.copy(disableScreenshots = it)
                        }
                    )
                }
                
                item {
                    SwitchSetting(
                        label = "Clear Clipboard on Exit",
                        checked = editedRule.clearClipboardOnExit,
                        onCheckedChange = {
                            editedRule = editedRule.copy(clearClipboardOnExit = it)
                        }
                    )
                }
                
                item {
                    SwitchSetting(
                        label = "Block Notifications",
                        checked = editedRule.blockNotifications,
                        onCheckedChange = {
                            editedRule = editedRule.copy(blockNotifications = it)
                        }
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
private fun SliderSetting(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
        }
        
        if (enabled) {
            Slider(
                value = if (value < 0) 50f else value,
                onValueChange = onValueChange,
                valueRange = 0f..100f,
                steps = 20
            )
            Text(
                "${if (value < 0) 50 else value.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SwitchSetting(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun EmptyRulesState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.AppSettingsAlt,
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
            "No App Behavior Rules",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Create rules to automatically adjust settings when specific apps are opened",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
