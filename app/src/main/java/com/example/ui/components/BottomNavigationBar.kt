package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LuqaPrimary
import com.example.ui.theme.LuqaSurfaceContainerLow
import com.example.viewmodel.NavDestination

@Composable
fun BottomNavigationBar(
    currentNav: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = LuqaSurfaceContainerLow,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            containerColor = Color.White,
            tonalElevation = 0.dp
        ) {
            val items = listOf(
                Triple(NavDestination.VENTAS, "Ventas", Icons.Filled.ShoppingCart to Icons.Outlined.ShoppingCart),
                Triple(NavDestination.HISTORIAL, "Historial", Icons.Filled.ReceiptLong to Icons.Outlined.ReceiptLong),
                Triple(NavDestination.CUADRE, "Cuadre", Icons.Filled.Dashboard to Icons.Outlined.Dashboard),
                Triple(NavDestination.INVENTARIO, "Inventario", Icons.Filled.Inventory2 to Icons.Outlined.Inventory2)
            )

            items.forEach { (destination, label, icons) ->
                val isSelected = currentNav == destination
                val testTag = when (destination) {
                    NavDestination.VENTAS -> "nav_ventas"
                    NavDestination.HISTORIAL -> "nav_historial"
                    NavDestination.CUADRE -> "nav_cuadre"
                    NavDestination.INVENTARIO -> "nav_inventario"
                }

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onNavigate(destination) },
                    modifier = Modifier.testTag(testTag),
                    icon = {
                        Icon(
                            imageVector = if (isSelected) icons.first else icons.second,
                            contentDescription = label,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LuqaPrimary,
                        selectedTextColor = LuqaPrimary,
                        unselectedIconColor = Color(0xFF737784),
                        unselectedTextColor = Color(0xFF737784),
                        indicatorColor = LuqaPrimary.copy(alpha = 0.12f)
                    )
                )
            }
        }
    }
}
