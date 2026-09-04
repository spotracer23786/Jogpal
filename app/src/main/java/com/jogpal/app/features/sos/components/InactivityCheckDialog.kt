package com.jogpal.app.features.sos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InactivityCheckDialog(
    onConfirmOk: () -> Unit,
    onActivateSOS: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onConfirmOk,
        containerColor = Color(0xFF18181B),
        shape = RoundedCornerShape(20.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "❓",
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Are you okay?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                text = "You haven't moved for a while during your active jog.",
                color = Color(0xFFA1A1AA),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmOk,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("I'm OK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onActivateSOS,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Activate SOS", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    )
}
