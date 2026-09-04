package com.jogpal.app.features.passport

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jogpal.app.core.designsystem.components.JogpalLogo
import com.jogpal.app.domain.passport.PassportData
import com.jogpal.app.ui.theme.JogpalPrimary
import java.util.Locale

@Composable
fun PassportShareCard(
    passport: PassportData,
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
        color = Color(0xFF0C1017)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            JogpalLogo()

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = JogpalPrimary.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "JOGPAL PASSPORT • LEVEL ${passport.level}",
                    fontWeight = FontWeight.Black,
                    color = JogpalPrimary,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = String.format(Locale.US, "%.1f KM", passport.totalDistanceKm),
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ShareMetricPill(label = "JOGS", value = "${passport.totalJogs}")
                ShareMetricPill(label = "STREAK", value = "🔥 ${passport.streakDays} DAYS")
                ShareMetricPill(label = "RECORDS", value = "🏆 ${passport.personalRecordCount}")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF161E2B)
            ) {
                Text(
                    text = "“Keep moving.”",
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
fun ShareMetricPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        Text(label, fontSize = 10.sp, color = Color.Gray, letterSpacing = 1.sp)
    }
}

fun sharePassportCardResult(context: Context, passport: PassportData) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "🏅 Check out my Jogpal Passport! Level ${passport.level} • ${String.format(Locale.US, "%.1f", passport.totalDistanceKm)} KM Total • ${passport.totalJogs} Jogs • 🔥 ${passport.streakDays} Day Streak! #JogpalPassport #KeepMoving"
        )
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Jogpal Passport"))
}
