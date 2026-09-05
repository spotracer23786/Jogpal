package com.jogpal.app.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem("home", "HOME", Icons.Filled.Home, Icons.Outlined.Home)
    object Ranks : BottomNavItem("nearby_runners", "RANKS", Icons.Filled.TrendingUp, Icons.Outlined.TrendingUp)
    object History : BottomNavItem("run_history", "HISTORY", Icons.Filled.History, Icons.Outlined.History)
    object Profile : BottomNavItem("passport", "PROFILE", Icons.Filled.Person, Icons.Outlined.Person)
    object Settings : BottomNavItem("safety_settings", "SETTINGS", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun JogpalBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSparkleClick: (() -> Unit)? = null
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Ranks,
        BottomNavItem.History,
        BottomNavItem.Profile,
        BottomNavItem.Settings
    )

    val neonColor = Color(0xFFC8FF00)

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Floating Sparkle Button on top right of nav bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 20.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(8.dp, CircleShape, spotColor = neonColor)
                        .clip(CircleShape)
                        .background(Color(0xFF262626))
                        .clickable {
                            if (onSparkleClick != null) {
                                onSparkleClick()
                            } else {
                                onNavigate("ghost_select")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "AI Sparkle",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Bottom Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .background(Color(0xFF0F0F0F))
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route

                    val animatedColor by animateColorAsState(
                        targetValue = if (isSelected) neonColor else Color(0xFF7A7A7A),
                        label = "NavColor"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onNavigate(item.route) }
                            .padding(vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title,
                            tint = animatedColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.title,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = animatedColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
