package com.lqanh.todoandroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lqanh.todoandroid.data.NavigationTab
import com.lqanh.todoandroid.ui.theme.OnSurfaceVariant
import com.lqanh.todoandroid.ui.theme.Primary
import com.lqanh.todoandroid.ui.theme.PrimaryContainer
import com.lqanh.todoandroid.ui.theme.Surface
import com.lqanh.todoandroid.ui.theme.SurfaceContainer

@Composable
fun BottomNavBar(
    currentTab: NavigationTab,
    onSelectTab: (NavigationTab) -> Unit
) {
    val items = listOf(
        Triple(NavigationTab.CONG_VIEC, "Công Việc", Icons.Default.CheckCircle),
        Triple(NavigationTab.LICH_TRINH, "Lịch Trình", Icons.Default.CalendarMonth),
        Triple(NavigationTab.THONG_KE, "Thống Kê", Icons.Default.Insights),
        Triple(NavigationTab.CAI_DAT, "Cài Đặt", Icons.Default.Settings)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface.copy(alpha = 0.95f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (tab, label, icon) ->
            NavItem(
                label = label,
                icon = icon,
                selected = currentTab == tab,
                onClick = { onSelectTab(tab) }
            )
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (selected) PrimaryContainer.copy(alpha = 0.15f) else Surface)
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Primary else OnSurfaceVariant
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Primary else OnSurfaceVariant
        )
    }
}
