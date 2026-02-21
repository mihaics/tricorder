package com.sysop.tricorder.permission

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sysop.tricorder.core.model.SensorCategory
import com.sysop.tricorder.core.ui.theme.AccentGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(
    onAllGranted: () -> Unit = {},
) {
    val context = LocalContext.current
    val categories = remember { PermissionManager.categoriesRequiringPermissions() }

    // Track permission states per category
    var permissionStates by remember {
        mutableStateOf(
            categories.associateWith { category ->
                PermissionManager.getPermissionsForCategory(category).all { permission ->
                    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                }
            }
        )
    }

    fun refreshPermissions() {
        permissionStates = categories.associateWith { category ->
            PermissionManager.getPermissionsForCategory(category).all { permission ->
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    val allGranted = permissionStates.values.all { it }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { _ ->
        refreshPermissions()
        if (permissionStates.values.all { it }) {
            onAllGranted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Permissions", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = AccentGreen,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Tricorder needs the following permissions to access device sensors and provide full functionality.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            // Grant All button
            item {
                Button(
                    onClick = {
                        permissionLauncher.launch(PermissionManager.allPermissions().toTypedArray())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !allGranted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = AccentGreen.copy(alpha = 0.3f),
                    ),
                ) {
                    Text(
                        text = if (allGranted) "All Permissions Granted" else "Grant All Permissions",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }

            // Per-category permission groups
            items(categories) { category ->
                val granted = permissionStates[category] ?: false
                PermissionGroupCard(
                    category = category,
                    granted = granted,
                    onGrant = {
                        val permissions = PermissionManager.getPermissionsForCategory(category)
                        permissionLauncher.launch(permissions.toTypedArray())
                    },
                )
            }

            // Continue button when all granted
            if (allGranted) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onAllGranted,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGreen,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text("Continue", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PermissionGroupCard(
    category: SensorCategory,
    granted: Boolean,
    onGrant: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Category icon
            Icon(
                imageVector = categoryIcon(category),
                contentDescription = category.displayName,
                tint = if (granted) AccentGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp),
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Category info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (granted) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                        contentDescription = if (granted) "Granted" else "Denied",
                        tint = if (granted) AccentGreen else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp),
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = PermissionManager.getExplanation(category),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            // Grant button per group
            if (!granted) {
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onGrant,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGreen),
                ) {
                    Text("Grant", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

private fun categoryIcon(category: SensorCategory): ImageVector = when (category) {
    SensorCategory.LOCATION -> Icons.Filled.LocationOn
    SensorCategory.RF -> Icons.Filled.Bluetooth
    SensorCategory.CAMERA -> Icons.Filled.CameraAlt
    SensorCategory.AUDIO -> Icons.Filled.Mic
    SensorCategory.MOTION -> Icons.Filled.DirectionsRun
    else -> Icons.Filled.Sensors
}
