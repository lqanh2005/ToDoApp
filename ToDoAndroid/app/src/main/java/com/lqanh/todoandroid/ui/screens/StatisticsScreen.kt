package com.lqanh.todoandroid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lqanh.todoandroid.data.Task
import com.lqanh.todoandroid.data.TaskStatus
import com.lqanh.todoandroid.ui.theme.Amber
import com.lqanh.todoandroid.ui.theme.Error
import com.lqanh.todoandroid.ui.theme.ErrorContainer
import com.lqanh.todoandroid.ui.theme.OnSurface
import com.lqanh.todoandroid.ui.theme.OnSurfaceVariant
import com.lqanh.todoandroid.ui.theme.Primary
import com.lqanh.todoandroid.ui.theme.PrimaryFixed
import com.lqanh.todoandroid.ui.theme.Secondary
import com.lqanh.todoandroid.ui.theme.Surface
import com.lqanh.todoandroid.ui.theme.SurfaceContainerLow
import com.lqanh.todoandroid.ui.theme.Tertiary

@Composable
fun StatisticsScreen(tasks: List<Task>) {
    val total = tasks.size
    val done = tasks.count { it.status == TaskStatus.DONE }
    val doing = tasks.count { it.status == TaskStatus.DOING }
    val todo = tasks.count { it.status == TaskStatus.TODO }
    val overdue = tasks.count { it.isOverdue && it.status != TaskStatus.DONE }
    val rate = if (total == 0) 0 else ((done * 100f) / total).toInt()

    val work = tasks.count { it.category == "work" }
    val personal = tasks.count { it.category == "personal" }
    val study = tasks.count { it.category == "study" }
    val shopping = tasks.count { it.category == "shopping" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 100.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("BÁO CÁO HIỆU SUẤT", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Primary)
                    Text("Tổng quan tuần này", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                }
                Text(
                    "Tuần 26",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(PrimaryFixed)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLow)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Primary)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("$rate%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("HOÀN THÀNH", color = Color.White.copy(0.8f), fontSize = 9.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Xuất sắc! Đã vượt 15% so với tuần trước", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Bạn đã xử lý thành công $done trên tổng số $total nhiệm vụ được giao.", fontSize = 12.sp, color = OnSurfaceVariant)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricBox("Đã hoàn thành", "$done", Tertiary, Modifier.weight(1f))
                MetricBox("Đang thực hiện", "$doing", Secondary, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricBox("Cần làm", "$todo", Primary, Modifier.weight(1f))
                MetricBox("Trễ hạn", "$overdue", Error, Modifier.weight(1f))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Phân bố theo danh mục", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            CategoryBar("💼 Công việc", work, total, Primary)
            CategoryBar("👤 Cá nhân", personal, total, Secondary)
            CategoryBar("📚 Học tập", study, total, Tertiary)
            CategoryBar("🛒 Mua sắm", shopping, total, Amber)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceContainerLow)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ErrorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocalFireDepartment, null, tint = Error)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Chuỗi 7 ngày liên tiếp!", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Bạn đã hoàn thành các nhiệm vụ quan trọng đúng giờ suốt 7 ngày qua.", fontSize = 12.sp, color = OnSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MetricBox(label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 11.sp, color = OnSurfaceVariant)
    }
}

@Composable
private fun CategoryBar(label: String, count: Int, total: Int, color: Color) {
    val pct = if (total == 0) 0 else ((count * 100f) / total).toInt()
    Column {
        Row {
            Text("$label ($count)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("$pct%", fontSize = 12.sp, color = OnSurfaceVariant)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { pct / 100f },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)),
            color = color,
            trackColor = SurfaceContainerLow
        )
    }
}
