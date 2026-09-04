package com.jogpal.app.features.passport

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jogpal.app.data.passport.PassportRepository
import com.jogpal.app.domain.passport.*
import com.jogpal.app.ui.theme.JogpalPrimary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassportScreen(
    onNavigateBack: () -> Unit,
    passportRepository: PassportRepository = remember { PassportRepository() }
) {
    val context = LocalContext.current
    val passportState by passportRepository.getPassportData().collectAsState(initial = PassportData("Runner"))

    var selectedRarityFilter by remember { mutableStateOf<RarityLevel?>(null) }
    var animatedStampScale by remember { mutableFloatStateOf(1.0f) }

    // Bounce animation effect for collectible stamps
    val animatedScale by animateFloatAsState(
        targetValue = animatedStampScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounce"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Jogpal Passport 🛂",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Your journey. Your places. Your achievements.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Digital Passport Collectible Header Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(JogpalPrimary.copy(alpha = 0.5f), Color(0xFF00E5FF).copy(alpha = 0.3f))),
                        RoundedCornerShape(24.dp)
                    ),
                color = Color(0xFF0F1520)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(JogpalPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🏃", fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = passportState.username,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = JogpalPrimary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "LEVEL ${passportState.level}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = JogpalPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = JogpalPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Key Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PassportStatPill(label = "JOGS", value = "${passportState.totalJogs}")
                        PassportStatPill(label = "TOTAL DISTANCE", value = String.format(Locale.US, "%.1f KM", passportState.totalDistanceKm))
                        PassportStatPill(label = "STREAK", value = "🔥 ${passportState.streakDays}d")
                        PassportStatPill(label = "RECORDS", value = "🏆 ${passportState.personalRecordCount}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Passport Completion Progress Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF141B26)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val percent = (passportState.unlockedMilestones.toFloat() / passportState.totalMilestones.toFloat()).coerceIn(0f, 1f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "PASSPORT COMPLETION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = JogpalPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${(percent * 100).toInt()}% • ${passportState.unlockedMilestones}/${passportState.totalMilestones} Unlocked",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { percent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = JogpalPrimary,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 3. Journey Progress Timeline
            SectionTitle("YOUR JOGPAL JOURNEY")
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF131A24)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    passportState.milestones.forEachIndexed { idx, milestone ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(
                                if (milestone.isCompleted) Icons.Default.CheckCircle else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (milestone.isCompleted) JogpalPrimary else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = milestone.title,
                                fontWeight = if (milestone.isCompleted) FontWeight.Bold else FontWeight.Normal,
                                color = if (milestone.isCompleted) Color.White else Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (milestone.completedDate != null) {
                                Text(
                                    text = milestone.completedDate,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        if (idx < passportState.milestones.size - 1) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 4. Location Stamps ("PLACES EXPLORED")
            SectionTitle("PLACES EXPLORED")
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(passportState.locationStamps) { stamp ->
                    Surface(
                        modifier = Modifier
                            .width(120.dp)
                            .scale(if (stamp.isUnlocked) animatedScale else 1.0f)
                            .clip(RoundedCornerShape(18.dp))
                            .border(
                                1.dp,
                                if (stamp.isUnlocked) JogpalPrimary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(18.dp)
                            )
                            .clickable {
                                if (stamp.isUnlocked) {
                                    animatedStampScale = 1.15f
                                }
                            },
                        color = if (stamp.isUnlocked) Color(0xFF16202C) else Color(0xFF0F141B)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(stamp.iconEmoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                stamp.cityName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (stamp.isUnlocked) Color.White else Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (stamp.isUnlocked) "${stamp.jogCount} JOGS" else "Locked",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (stamp.isUnlocked) JogpalPrimary else Color.DarkGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 5. Achievement Collection
            SectionTitle("ACHIEVEMENT COLLECTION")
            Spacer(modifier = Modifier.height(12.dp))

            // Rarity Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedRarityFilter == null,
                    onClick = { selectedRarityFilter = null },
                    label = { Text("ALL") }
                )
                RarityLevel.values().forEach { rarity ->
                    FilterChip(
                        selected = selectedRarityFilter == rarity,
                        onClick = { selectedRarityFilter = rarity },
                        label = { Text(rarity.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val filteredBadges = passportState.achievements.filter {
                    selectedRarityFilter == null || it.rarity == selectedRarityFilter
                }
                filteredBadges.forEach { badge ->
                    AchievementRow(badge = badge)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 6. Personal Records
            SectionTitle("PERSONAL RECORDS")
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF131A24)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    passportState.personalRecords.forEachIndexed { idx, record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(record.label, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                    if (record.isRecent) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = JogpalPrimary.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                "NEW",
                                                color = JogpalPrimary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(record.dateAchieved, fontSize = 11.sp, color = Color.Gray)
                            }
                            Text(
                                record.value,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = JogpalPrimary
                            )
                        }
                        if (idx < passportState.personalRecords.size - 1) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 7. Social Accomplishments
            SectionTitle("SOCIAL MILESTONES")
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                passportState.socialMilestones.take(2).forEach { social ->
                    Box(modifier = Modifier.weight(1f)) {
                        SocialMilestoneCard(social)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 8. Share Passport Button
            Button(
                onClick = {
                    sharePassportCardResult(context, passportState)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JogpalPrimary,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share My Passport", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    LaunchedEffect(animatedStampScale) {
        if (animatedStampScale > 1.0f) {
            kotlinx.coroutines.delay(300)
            animatedStampScale = 1.0f
        }
    }
}

@Composable
fun PassportStatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
        Text(label, fontSize = 9.sp, color = Color.Gray, letterSpacing = 1.sp)
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = JogpalPrimary,
        letterSpacing = 1.sp
    )
}

@Composable
fun AchievementRow(badge: AchievementBadge) {
    val rarityColor = when (badge.rarity) {
        RarityLevel.COMMON -> Color.Gray
        RarityLevel.RARE -> Color(0xFF4A90E2)
        RarityLevel.EPIC -> Color(0xFFB34AE2)
        RarityLevel.LEGENDARY -> Color(0xFFFFB300)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                1.dp,
                if (badge.isUnlocked) rarityColor.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(18.dp)
            ),
        color = if (badge.isUnlocked) Color(0xFF141C26) else Color(0xFF0D1219)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (badge.isUnlocked) rarityColor.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (badge.isUnlocked) badge.iconEmoji else "🔒",
                    fontSize = 22.sp
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        badge.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (badge.isUnlocked) Color.White else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = rarityColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            badge.rarity.name,
                            color = rarityColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    badge.description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun SocialMilestoneCard(social: SocialMilestone) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF141C26),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(social.iconEmoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(social.value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            Text(social.label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}
