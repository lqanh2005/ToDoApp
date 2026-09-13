package com.lqanh.todoandroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lqanh.todoandroid.data.AppAssets
import com.lqanh.todoandroid.ui.theme.OnSurface
import com.lqanh.todoandroid.ui.theme.OnSurfaceVariant
import com.lqanh.todoandroid.ui.theme.Primary
import com.lqanh.todoandroid.ui.theme.Surface
import com.lqanh.todoandroid.ui.theme.SurfaceContainer

@Composable
fun AppHeader(
    title: String,
    subtitle: String = "ToDoList",
    showBack: Boolean = false,
    showSearch: Boolean = false,
    onBack: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface.copy(alpha = 0.95f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBack) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = OnSurface)
            }
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Primary),
                contentAlignment = Alignment.Center
            ) {
                Text("NS", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(Modifier.width(10.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subtitle.uppercase(),
                color = Primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = title,
                color = OnSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (showSearch) {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Tìm kiếm", tint = OnSurfaceVariant)
            }
        }

        AsyncImage(
            model = AppAssets.PROFILE_AVATAR,
            contentDescription = "Hồ sơ",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable(onClick = onProfileClick)
                .background(SurfaceContainer)
        )
    }
}
