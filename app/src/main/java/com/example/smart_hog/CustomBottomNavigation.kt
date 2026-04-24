package com.example.smart_hog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val PrimaryOrange = Color(0xFFEF8E07) // Vibrant Orange
val DarkOrange = Color(0xFFD87D06)    // Darker Orange for selection
val LightOrange = Color(0xFFFFB347)   // Slightly Dark Orange for unselected

data class NavItem(
    val id: Int,
    val label: String,
    val icon: ImageVector
)

@Composable
fun CustomBottomNavigation(
    modifier: Modifier = Modifier,
    items: List<NavItem>,
    selectedId: Int,
    onItemSelected: (NavItem) -> Unit
) {
    Surface(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(72.dp),
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 12.dp // Highlights and shadows
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = selectedId == item.id
                
                BottomNavItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onItemSelected(item) }
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    // Animation for color
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) DarkOrange else LightOrange,
        label = "IconColor"
    )
    
    // Animation for size
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1.0f,
        label = "IconScale"
    )

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isSelected) PrimaryOrange.copy(alpha = 0.15f) else Color.Transparent,
                    shape = CircleShape
                )
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier
                    .size(24.dp)
                    .scale(iconScale)
            )
        }
        
        AnimatedVisibility(visible = isSelected) {
            Text(
                text = item.label,
                color = DarkOrange,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomBottomNavigationPreview() {
    val items = listOf(
        NavItem(1, "Dashboard", Icons.Outlined.WbSunny),
        NavItem(2, "Logs", Icons.Outlined.ListAlt),
        NavItem(3, "Home", Icons.Outlined.Home),
        NavItem(4, "Favorites", Icons.Outlined.FavoriteBorder),
        NavItem(5, "Stats", Icons.Outlined.BarChart)
    )
    
    var selectedId by remember { mutableStateOf(3) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.BottomCenter
    ) {
        CustomBottomNavigation(
            items = items,
            selectedId = selectedId,
            onItemSelected = { selectedId = it.id }
        )
    }
}
