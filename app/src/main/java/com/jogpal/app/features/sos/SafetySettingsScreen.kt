package com.jogpal.app.features.sos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jogpal.app.core.designsystem.components.NeonMeshCard

@Composable
fun SafetySettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onStartTestMode: () -> Unit
) {
    var locationSharingEnabled by remember { mutableStateOf(true) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Section 1: ACCOUNT
                Column {
                    Text(
                        text = "ACCOUNT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFC8FF00),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    NeonMeshCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0xFFC8FF00),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Row 1: Profile
                            SettingsRowItem(
                                icon = Icons.Default.Person,
                                title = "Profile",
                                subtitle = "View your runner profile",
                                onClick = onNavigateBack
                            )

                            HorizontalDivider(color = Color(0x22000000), thickness = 1.dp)

                            // Row 2: Edit Profile
                            SettingsRowItem(
                                icon = Icons.Default.Edit,
                                title = "Edit Profile",
                                subtitle = "Update your details and stats",
                                onClick = { showInfoDialog = "Edit Profile: Profile editing mode open." }
                            )

                            HorizontalDivider(color = Color(0x22000000), thickness = 1.dp)

                            // Row 3: Delete Account
                            SettingsRowItem(
                                icon = Icons.Default.Delete,
                                iconBgColor = Color(0xFF7A1C1C),
                                iconColor = Color(0xFFEF4444),
                                title = "Delete Account",
                                titleColor = Color(0xFF991B1B),
                                subtitle = "Permanently remove your data",
                                onClick = { showDeleteAccountDialog = true }
                            )
                        }
                    }
                }

                // Section 2: RUNNING
                Column {
                    Text(
                        text = "RUNNING",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFC8FF00),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    NeonMeshCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0xFFC8FF00),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Row 1: Running Preferences
                            SettingsRowItem(
                                icon = Icons.Default.DirectionsRun,
                                title = "Running\nPreferences",
                                subtitle = "Goals, experience, and\npace",
                                rightLabel = "M10.2: Future",
                                onClick = { showInfoDialog = "Running Preferences: Configured for optimal pacing." }
                            )

                            HorizontalDivider(color = Color(0x22000000), thickness = 1.dp)

                            // Row 2: Sharing Status
                            SettingsRowItem(
                                icon = Icons.Default.LocationOn,
                                title = "Sharing Status",
                                subtitle = "Current live tracking state",
                                rightLabel = "Active",
                                onClick = onStartTestMode
                            )
                        }
                    }
                }

                // Section 3: PRIVACY & SAFETY
                Column {
                    Text(
                        text = "PRIVACY & SAFETY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFC8FF00),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    NeonMeshCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0xFFC8FF00),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Row 1: Location Sharing Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0x33000000)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column {
                                        Text(
                                            text = "Location Sharing",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Allow partners to see your live position",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF333333)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Switch(
                                    checked = locationSharingEnabled,
                                    onCheckedChange = { locationSharingEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Black,
                                        checkedTrackColor = Color(0x44000000),
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color(0x22000000)
                                    )
                                )
                            }

                            HorizontalDivider(color = Color(0x22000000), thickness = 1.dp)

                            // Row 2: Privacy Information
                            SettingsRowItem(
                                icon = Icons.Default.Lock,
                                title = "Privacy Information",
                                subtitle = "Shine user safety controls",
                                onClick = { showInfoDialog = "Privacy Information: Your data is encrypted and strictly private." }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = Color(0xFF181818),
            title = {
                Text("Delete Account", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to permanently delete your account? All saved runs, streaks, and personal stats will be removed.",
                    color = Color.White,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // Information Modal Dialog
    showInfoDialog?.let { infoText ->
        AlertDialog(
            onDismissRequest = { showInfoDialog = null },
            containerColor = Color(0xFF181818),
            title = {
                Text("Settings Information", color = Color(0xFFC8FF00), fontWeight = FontWeight.Bold)
            },
            text = {
                Text(infoText, color = Color.White, fontSize = 14.sp)
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = null }) {
                    Text("OK", color = Color(0xFFC8FF00), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconBgColor: Color = Color(0x33000000),
    iconColor: Color = Color.Black,
    titleColor: Color = Color.Black,
    rightLabel: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = titleColor,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    lineHeight = 14.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (rightLabel != null) {
                Text(
                    text = rightLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (titleColor == Color(0xFF991B1B)) Color(0xFF991B1B) else Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
