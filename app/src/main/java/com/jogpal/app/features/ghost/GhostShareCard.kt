package com.jogpal.app.features.ghost

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jogpal.app.core.designsystem.components.JogpalLogo
import com.jogpal.app.ui.theme.JogpalPrimary
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@Composable
fun GhostShareCard(
    distanceKm: Double,
    todayTimeStr: String,
    timeDiffStr: String,
    isWinner: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .border(
                2.dp,
                Brush.horizontalGradient(listOf(JogpalPrimary, Color(0xFF00E5FF))),
                RoundedCornerShape(28.dp)
            ),
        color = Color(0xFF0D121B)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Jogpal Branding
            JogpalLogo()

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isWinner) JogpalPrimary.copy(alpha = 0.2f) else Color(0xFFFF5252).copy(alpha = 0.2f)
            ) {
                Text(
                    text = if (isWinner) "👻 GHOST DEFEATED" else "👻 GHOST CHALLENGE",
                    fontWeight = FontWeight.Black,
                    color = if (isWinner) JogpalPrimary else Color(0xFFFF5252),
                    fontSize = 13.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = String.format(Locale.US, "%.2f KM", distanceKm),
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Text(
                text = todayTimeStr,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = JogpalPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF18202C)
            ) {
                Text(
                    text = if (isWinner) "$timeDiffStr FASTER THAN MY LAST RUN" else "COMPETED AGAINST PAST SELF",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Track & compete on Jogpal App",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

fun shareGhostCardResult(context: Context, distanceKm: Double, todayTimeStr: String, timeDiffStr: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "👻 I just defeated my Ghost on Jogpal! Ran ${String.format(Locale.US, "%.2f", distanceKm)} km in $todayTimeStr ($timeDiffStr faster!). #Jogpal #GhostMode"
        )
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Ghost Result"))
}
