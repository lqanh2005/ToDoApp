package com.lqanh.todoandroid.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lqanh.todoandroid.ui.theme.Primary

@Composable
fun AppFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        containerColor = Primary,
        contentColor = Color.White,
        modifier = Modifier.size(56.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = "Thêm công việc")
    }
}
