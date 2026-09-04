package com.jogpal.app.features.sos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jogpal.app.features.sos.SOSEvent
import com.jogpal.app.features.sos.SOSEventType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SOSHistoryScreen(
    events: List<SOSEvent>,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safety History", color = Color.White, fontWeight = FontWeight.Bold) },
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
        ) {
            Text(
                text = "Previous SOS activations, cancellations, and test runs are logged separately from regular jogging history.",
                color = Color(0xFFA1A1AA),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (events.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No safety events recorded.", color = Color(0xFF71717A), fontSize = 14.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(events) { event ->
                        SOSEventItem(event = event)
                    }
                }
            }
        }
    }
}

@Composable
private fun SOSEventItem(event: SOSEvent) {
    val (badgeColor, badgeText) = when (event.type) {
        SOSEventType.ACTIVE_SOS -> Color(0xFFEF4444) to "ACTIVE"
        SOSEventType.TEST_MODE -> Color(0xFFFBBF24) to "TEST MODE"
        SOSEventType.CANCELLED_SOS -> Color(0xFFA1A1AA) to "CANCELLED"
    }

    Card(
        shape = RoundedCornerShape(14.dp),
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = event.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Box(
                        modifier = Modifier
                            .background(badgeColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }
                Text(
                    text = event.dateString,
                    color = Color(0xFFA1A1AA),
                    fontSize = 12.sp
                )
                Text(
                    text = "Location: ${event.location}",
                    color = Color(0xFF71717A),
                    fontSize = 11.sp
                )
            }

            if (event.type == SOSEventType.ACTIVE_SOS) {
                Text(
                    text = "${event.contactsNotifiedCount} notified",
                    color = Color(0xFF22C55E),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
