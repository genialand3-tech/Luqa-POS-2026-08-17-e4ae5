package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.LuqaPrimary
import com.example.ui.theme.LuqaSurfaceContainerLow
import com.example.ui.theme.LuqaSurfaceContainerHighest
import com.example.viewmodel.NavDestination

@Composable
fun SideNavigationRail(
    currentNav: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(96.dp)
            .fillMaxHeight(),
        color = LuqaSurfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Logo & Brand
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(LuqaPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "L",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Luqa",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = LuqaPrimary
                )
                Text(
                    text = "V1.0",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Navigation Tabs
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NavItem(
                    destination = NavDestination.VENTAS,
                    currentNav = currentNav,
                    label = "Ventas",
                    filledIcon = Icons.Filled.ShoppingCart,
                    outlinedIcon = Icons.Outlined.ShoppingCart,
                    onNavigate = onNavigate,
                    testTag = "nav_ventas"
                )

                NavItem(
                    destination = NavDestination.CUADRE,
                    currentNav = currentNav,
                    label = "Cuadre",
                    filledIcon = Icons.Filled.Dashboard,
                    outlinedIcon = Icons.Outlined.Dashboard,
                    onNavigate = onNavigate,
                    testTag = "nav_cuadre"
                )

                NavItem(
                    destination = NavDestination.INVENTARIO,
                    currentNav = currentNav,
                    label = "Inventario",
                    filledIcon = Icons.Filled.Inventory2,
                    outlinedIcon = Icons.Outlined.Inventory2,
                    onNavigate = onNavigate,
                    testTag = "nav_inventario"
                )
            }

            // Merchant Profile Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            ) {
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAuQiZTd6PGJLUH7ScvIiM8HXEZBsklB0Qe9gExiCCqMmiS1khaL0KItP06AbeI-hIWgUksqJvO3TBYpu_38qn-cTTX3m_AmlELaCR14zYJZAW5qOEtvnQ08ZkuOtfhAFvDYoZY0BTKqFstx_hI6DOL0uVN7APl-VQQm0DgSQk-mbDm4wuzNRwMstjzPenbGCgtONL5-MAk5wqrIqeZm9cyTGd_KzvhEFJ9dgWBbwiiiQ1vURRfokkPDicEqY7LHwiErJrbE2C4CYNL",
                    contentDescription = "Merchant Profile",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    destination: NavDestination,
    currentNav: NavDestination,
    label: String,
    filledIcon: ImageVector,
    outlinedIcon: ImageVector,
    onNavigate: (NavDestination) -> Unit,
    testTag: String
) {
    val isSelected = destination == currentNav
    val backgroundColor by animateColorAsState(
        if (isSelected) LuqaSurfaceContainerHighest else Color.Transparent,
        label = "nav_bg"
    )
    val contentColor by animateColorAsState(
        if (isSelected) LuqaPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "nav_color"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onNavigate(destination) }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(LuqaPrimary)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) filledIcon else outlinedIcon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
