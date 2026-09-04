package com.jogpal.app.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jogpal.app.ui.theme.*

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isCenterButton: Boolean = false
) {
    object Home : BottomNavItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Matching : BottomNavItem("nearby_runners", "Runners", Icons.Filled.People, Icons.Outlined.People)
    object Ghost : BottomNavItem("ghost_select", "Ghost", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, isCenterButton = true)
    object History : BottomNavItem("run_history", "History", Icons.Filled.History, Icons.Outlined.History)
    object Profile : BottomNavItem("passport", "Passport", Icons.Filled.Badge, Icons.Outlined.Badge)
}

@Composable
fun JogpalBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Matching,
        BottomNavItem.Ghost,
        BottomNavItem.History,
        BottomNavItem.Profile
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating glass capsule container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(16.dp, shape = RoundedCornerShape(32.dp), spotColor = Color(0x330F7A60))
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEEFFFFFF),
                            Color(0xFDF6FBF9)
                        )
                    )
                )
                .border(1.dp, JogpalCardBorderLight, RoundedCornerShape(32.dp))
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route

                if (item.isCenterButton) {
                    // Center Floating Action Sparkle Button (as seen in Ref UI 1 & 2)
                    Box(
                        modifier = Modifier
                            .offset(y = (-6).dp)
                            .size(52.dp)
                            .shadow(12.dp, CircleShape, spotColor = JogpalPrimary)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        JogpalPrimary,
                                        JogpalSecondary,
                                        JogpalTertiary
                                    )
                                )
                            )
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { onNavigate(item.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = item.title,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                } else {
                    val animatedIconTint by animateColorAsState(
                        targetValue = if (isSelected) JogpalPrimary else JogpalMutedTextLight,
                        label = "IconTint"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigate(item.route) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(JogpalPillBgLight)
                                )
                            }
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                tint = animatedIconTint,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.title,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = animatedIconTint
                        )
                    }
                }
            }
        }
    }
}
