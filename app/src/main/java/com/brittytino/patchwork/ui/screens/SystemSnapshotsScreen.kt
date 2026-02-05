package com.brittytino.patchwork.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brittytino.patchwork.R
import com.brittytino.patchwork.data.database.entity.SystemSnapshot
import com.brittytino.patchwork.viewmodels.SystemSnapshotViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemSnapshotsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SystemSnapshotViewModel = viewModel()
) {
    val snapshots by viewModel.snapshots.collectAsState()
    val quickAccessSnapshots by viewModel.quickAccessSnapshots.collectAsState()
    val showCreateDialog by viewModel.showCreateDialog.collectAsState()
    val selectedSnapshot by viewModel.selectedSnapshot.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf<SystemSnapshot?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Profiles") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showCreateDialog() },
                icon = { Icon(Icons.Default.CameraAlt, null) },
                text = { Text("Capture Current") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Info Card
            item {
                InfoCard()
            }
            
            // Quick Access Section
            if (quickAccessSnapshots.isNotEmpty()) {
                item {
                    Text(
                        "Quick Access",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(quickAccessSnapshots) { snapshot ->
                            SnapshotQuickCard(
                                snapshot = snapshot,
                                onRestore = { viewModel.restoreSnapshot(snapshot) },
                                onClick = { viewModel.showSnapshotDetails(snapshot) }
                            )
                        }
                    }
                }
                
                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
            
            // All Snapshots
            item {
                Text(
                    if (quickAccessSnapshots.isEmpty()) "All Profiles" else "Other Profiles",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            if (snapshots.isEmpty()) {
                item {
                    EmptySnapshotsState(
                        onCreate = { viewModel.showCreateDialog() }
                    )
                }
            } else {
                items(snapshots) { snapshot ->
                    SnapshotListItem(
                        snapshot = snapshot,
                        onRestore = { viewModel.restoreSnapshot(snapshot) },
                        onClick = { viewModel.showSnapshotDetails(snapshot) },
                        onDelete = { showDeleteDialog = snapshot },
                        onToggleQuickAccess = { viewModel.toggleQuickAccess(snapshot) }
                    )
                }
            }
        }
    }
    
    // Create Dialog
    if (showCreateDialog) {
        CreateSnapshotDialog(
            onDismiss = { viewModel.hideCreateDialog() },
            onCreate = { name, description ->
                viewModel.createSnapshot(name, description)
            }
        )
    }
    
    // Details Sheet
    selectedSnapshot?.let { snapshot ->
        SnapshotDetailsSheet(
            snapshot = snapshot,
            onDismiss = { viewModel.hideSnapshotDetails() },
            onRestore = { 
                viewModel.restoreSnapshot(it)
                viewModel.hideSnapshotDetails()
            },
            onDelete = {
                showDeleteDialog = it
                viewModel.hideSnapshotDetails()
            },
            onToggleQuickAccess = { viewModel.toggleQuickAccess(it) }
        )
    }
    
    // Delete Confirmation
    showDeleteDialog?.let { snapshot ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Profile?") },
            text = { Text("Delete '${snapshot.name}'? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSnapshot(snapshot)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InfoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "System State Snapshots",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Save and restore complete system configurations. Perfect for Work/Home/Sleep modes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun SnapshotQuickCard(
    snapshot: SystemSnapshot,
    onRestore: (SystemSnapshot) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painterResource(getSnapshotIcon(snapshot.name)),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                snapshot.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            if (snapshot.description.isNotEmpty()) {
                Text(
                    snapshot.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onRestore(snapshot) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Restore")
            }
        }
    }
}

@Composable
fun SnapshotListItem(
    snapshot: SystemSnapshot,
    onRestore: (SystemSnapshot) -> Unit,
    onClick: () -> Unit,
    onDelete: (SystemSnapshot) -> Unit,
    onToggleQuickAccess: (SystemSnapshot) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(getSnapshotIcon(snapshot.name)),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    snapshot.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (snapshot.description.isNotEmpty()) {
                    Text(
                        snapshot.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                snapshot.lastUsedAt?.let {
                    Text(
                        "Last used ${formatTimeAgo(it)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            IconButton(onClick = { onRestore(snapshot) }) {
                Icon(Icons.Default.PlayArrow, "Restore")
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "More")
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { 
                            Text(if (snapshot.isQuickAccess) "Remove from Quick Access" else "Add to Quick Access") 
                        },
                        onClick = {
                            onToggleQuickAccess(snapshot)
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                if (snapshot.isQuickAccess) Icons.Default.StarBorder else Icons.Default.Star,
                                null
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDelete(snapshot)
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, null) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnapshotDetailsSheet(
    snapshot: SystemSnapshot,
    onDismiss: () -> Unit,
    onRestore: (SystemSnapshot) -> Unit,
    onDelete: (SystemSnapshot) -> Unit,
    onToggleQuickAccess: (SystemSnapshot) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    snapshot.name,
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = { onToggleQuickAccess(snapshot) }) {
                    Icon(
                        if (snapshot.isQuickAccess) Icons.Default.Star else Icons.Default.StarBorder,
                        "Quick Access",
                        tint = if (snapshot.isQuickAccess) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                "Created ${formatDate(snapshot.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Show saved settings
            Text("Saved Settings:", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            
            snapshot.mediaVolume?.let {
                SettingItem("Media Volume", "$it")
            }
            snapshot.brightness?.let {
                SettingItem("Brightness", "${(it * 100 / 255)}%")
            }
            snapshot.screenTimeout?.let {
                SettingItem("Screen Timeout", "${it / 1000}s")
            }
            snapshot.rotationLocked?.let {
                SettingItem("Rotation", if (it) "Locked" else "Auto")
            }
            
            Spacer(Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onDelete(snapshot) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete")
                }
                
                Button(
                    onClick = { onRestore(snapshot) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Restore")
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CreateSnapshotDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Snapshot") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (name.isNotBlank()) {
                        onCreate(name.trim(), description.trim())
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
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
fun EmptySnapshotsState(onCreate: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                "No Profiles Yet",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Capture your current system state to restore it later",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onCreate) {
                Text("Create First Profile")
            }
        }
    }
}

// Helper functions
fun getSnapshotIcon(name: String): Int {
    return when {
        name.contains("work", ignoreCase = true) -> R.drawable.rounded_settings_24
        name.contains("home", ignoreCase = true) -> R.drawable.rounded_settings_24
        name.contains("sleep", ignoreCase = true) || name.contains("night", ignoreCase = true) -> R.drawable.rounded_nightlight_24
        name.contains("travel", ignoreCase = true) -> R.drawable.rounded_location_on_24
        name.contains("meeting", ignoreCase = true) -> R.drawable.rounded_settings_24
        else -> R.drawable.rounded_settings_24
    }
}

fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> "${diff / 86400_000}d ago"
    }
}

fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    return java.text.SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", java.util.Locale.getDefault()).format(date)
}
