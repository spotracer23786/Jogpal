package com.jogpal.app.features.sos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jogpal.app.features.sos.EmergencyProfile

@Composable
fun EmergencyInfoCard(
    profile: EmergencyProfile,
    onSaveProfile: (EmergencyProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf(profile.importantNotes) }
    var lang by remember { mutableStateOf(profile.preferredLanguage) }
    var medical by remember { mutableStateOf(profile.medicalInfo) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Emergency Profile",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Optional information provided by you",
                        color = Color(0xFFA1A1AA),
                        fontSize = 12.sp
                    )
                }
                TextButton(onClick = {
                    if (isEditing) {
                        onSaveProfile(profile.copy(importantNotes = notes, preferredLanguage = lang, medicalInfo = medical))
                    }
                    isEditing = !isEditing
                }) {
                    Text(
                        text = if (isEditing) "Save" else "Edit",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User-provided disclaimer banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF27272A), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "ℹ️ Clearly labeled: All items below are self-reported by user. Jogpal does not auto-generate medical data.",
                    color = Color(0xFFA1A1AA),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!isEditing) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoRow(label = "Preferred Emergency Language", value = profile.preferredLanguage)
                    InfoRow(label = "Important Notes / Instructions", value = profile.importantNotes.ifBlank { "None added" })
                    InfoRow(label = "Medical Information (Optional)", value = profile.medicalInfo.ifBlank { "None added" })
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = lang,
                        onValueChange = { lang = it },
                        label = { Text("Preferred Emergency Language", color = Color(0xFFA1A1AA)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Important Notes", color = Color(0xFFA1A1AA)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = medical,
                        onValueChange = { medical = it },
                        label = { Text("Medical Information (Self-Reported)", color = Color(0xFFA1A1AA)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(text = label, color = Color(0xFF71717A), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
