package com.jogpal.app.features.sos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jogpal.app.features.sos.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetySettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onStartTestMode: () -> Unit
) {
    val repository = remember { SOSRepository.getInstance() }
    val contacts by repository.trustedContacts.collectAsState()
    val emergencyProfile by repository.emergencyProfile.collectAsState()
    val inactivityEnabled by repository.inactivityCheckEnabled.collectAsState()

    var showAddContactDialog by remember { mutableStateOf(false) }
    var showPreviewDialog by remember { mutableStateOf(false) }

    val mockLiveLocation = remember { LiveLocationData() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFFEF4444))
                        Text("Safety & SOS", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF09090B))
            )
        },
        containerColor = Color(0xFF09090B)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Description
            Text(
                text = "If something goes wrong while you are jogging, Jogpal has a clear, immediate way to broadcast your live location to trusted contacts.",
                color = Color(0xFFA1A1AA),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            // Test SOS Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F46)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Test SOS Flow", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Understand the emergency process safely", color = Color(0xFFA1A1AA), fontSize = 12.sp)
                        }
                        Button(
                            onClick = onStartTestMode,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27272A)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Test SOS", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "TEST MODE — No contacts will be notified",
                        color = Color(0xFFFBBF24),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Safety Circle (Trusted Contacts)
            SafetyCircleCard(
                contacts = contacts,
                onToggleContact = { repository.toggleTrustedContact(it) },
                onAddContactClick = { showAddContactDialog = true }
            )

            // Automated Fall & Impact Detection Toggle Card
            var fallDetectionEnabled by remember { mutableStateOf(true) }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Automated Fall & Impact Guard", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFEF4444).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("AI SENSOR", color = Color(0xFFEF4444), fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Uses hardware accelerometer to trigger immediate emergency countdown upon sudden hard impacts or falls.",
                            color = Color(0xFFA1A1AA),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = fallDetectionEnabled,
                        onCheckedChange = { fallDetectionEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = com.jogpal.app.ui.theme.JogpalPrimary
                        )
                    )
                }
            }

            // Smart Inactivity Check Toggle Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Smart Inactivity Check", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Prompts 'Are you okay?' if you haven't moved for an extended duration during an active jog.",
                            color = Color(0xFFA1A1AA),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = inactivityEnabled,
                        onCheckedChange = { repository.toggleInactivityCheck(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF38BDF8)
                        )
                    )
                }
            }

            // Emergency Information Profile
            EmergencyInfoCard(
                profile = emergencyProfile,
                onSaveProfile = { repository.updateEmergencyProfile(it) }
            )

            // SMS Alert Preview Row
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPreviewDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("SMS Alert Preview", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("See exact message format sent to trusted contacts", color = Color(0xFFA1A1AA), fontSize = 12.sp)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFA1A1AA))
                }
            }

            // Safety History Link Row
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToHistory() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Safety History", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Log of past SOS activations and test runs", color = Color(0xFFA1A1AA), fontSize = 12.sp)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFA1A1AA))
                }
            }
        }
    }

    if (showAddContactDialog) {
        AddTrustedContactDialog(
            onAdd = { contact ->
                repository.addTrustedContact(contact)
                showAddContactDialog = false
            },
            onDismiss = { showAddContactDialog = false }
        )
    }

    if (showPreviewDialog) {
        SOSNotificationPreviewDialog(
            locationData = mockLiveLocation,
            onDismiss = { showPreviewDialog = false }
        )
    }
}
