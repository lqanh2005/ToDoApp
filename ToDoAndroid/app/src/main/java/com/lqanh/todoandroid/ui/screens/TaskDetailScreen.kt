package com.lqanh.todoandroid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.lqanh.todoandroid.data.AppAssets
import com.lqanh.todoandroid.data.PriorityLevel
import com.lqanh.todoandroid.data.Subtask
import com.lqanh.todoandroid.data.Task
import com.lqanh.todoandroid.data.TaskStatus
import com.lqanh.todoandroid.ui.theme.Amber
import com.lqanh.todoandroid.ui.theme.Error
import com.lqanh.todoandroid.ui.theme.ErrorContainer
import com.lqanh.todoandroid.ui.theme.OnErrorContainer
import com.lqanh.todoandroid.ui.theme.OnSurface
import com.lqanh.todoandroid.ui.theme.OnSurfaceVariant
import com.lqanh.todoandroid.ui.theme.Outline
import com.lqanh.todoandroid.ui.theme.Primary
import com.lqanh.todoandroid.ui.theme.PrimaryFixed
import com.lqanh.todoandroid.ui.theme.Secondary
import com.lqanh.todoandroid.ui.theme.Surface
import com.lqanh.todoandroid.ui.theme.SurfaceContainer
import com.lqanh.todoandroid.ui.theme.SurfaceContainerHigh
import com.lqanh.todoandroid.ui.theme.SurfaceContainerLow
import com.lqanh.todoandroid.ui.theme.Tertiary
import com.lqanh.todoandroid.ui.theme.TertiaryFixed

@Composable
fun TaskDetailScreen(
    task: Task,
    onUpdate: (Task) -> Unit,
    onDelete: (String) -> Unit,
    onBack: () -> Unit
) {
    var showDelete by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var editTitle by remember(task.id) { mutableStateOf(task.title) }
    var editPriority by remember(task.id) { mutableStateOf(task.priority) }
    var previewUrl by remember { mutableStateOf<String?>(null) }
    var showAddSub by remember { mutableStateOf(false) }
    var newSubTitle by remember { mutableStateOf("") }

    val completed = task.subtasks.count { it.completed }
    val total = task.subtasks.size
    val progress = if (total == 0) 0 else ((completed * 100f) / total).toInt()
    val images = task.images?.takeIf { it.isNotEmpty() } ?: listOf(
        com.lqanh.todoandroid.data.TaskImage(AppAssets.SHOWCASE_IMG_1, "Mockup Wireframe v2"),
        com.lqanh.todoandroid.data.TaskImage(AppAssets.SHOWCASE_IMG_2, "Bảng màu & Design Token")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                task.code,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Primary,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(PrimaryFixed)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(task.sprint ?: "Sprint 24", fontSize = 12.sp, color = OnSurfaceVariant, modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceContainer)
                    .clickable { showEdit = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Edit, null, tint = Primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Chỉnh sửa", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(6.dp))
            IconButton(
                onClick = { showDelete = true },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ErrorContainer)
            ) {
                Icon(Icons.Default.Delete, null, tint = OnErrorContainer, modifier = Modifier.size(18.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceContainerHigh.copy(0.7f))
                .padding(6.dp)
        ) {
            StatusButton("Cần làm", Icons.Default.RadioButtonUnchecked, task.status == TaskStatus.TODO, Modifier.weight(1f)) {
                onUpdate(task.copy(status = TaskStatus.TODO))
            }
            StatusButton("Đang làm", null, task.status == TaskStatus.DOING, Modifier.weight(1f), showDot = true) {
                onUpdate(task.copy(status = TaskStatus.DOING))
            }
            StatusButton("Hoàn thành", Icons.Default.CheckCircle, task.status == TaskStatus.DONE, Modifier.weight(1f), iconTint = Tertiary) {
                onUpdate(task.copy(status = TaskStatus.DONE, completedAt = task.completedAt ?: "17:00"))
            }
        }

        if (task.isOverdue && task.status != TaskStatus.DONE) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ErrorContainer)
                    .padding(12.dp)
            ) {
                Icon(Icons.Default.Warning, null, tint = Error)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("CẢNH BÁO TRỄ HẠN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Error)
                    Text(task.overdueText ?: "Đã quá hạn so với tiến độ cam kết.", fontSize = 12.sp, color = OnErrorContainer)
                }
            }
        }

        if (task.status == TaskStatus.DONE) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(TertiaryFixed.copy(0.4f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Verified, null, tint = Tertiary)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Công việc đã hoàn thành!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Tertiary)
                    Text("Toàn bộ các đầu mục đã được nghiệm thu.", fontSize = 11.sp, color = OnSurfaceVariant)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Badge(Icons.Default.Work, task.categoryLabel, Primary, SurfaceContainer)
                Badge(
                    Icons.Default.Flag,
                    when (task.priority) {
                        PriorityLevel.CAO -> "Ưu tiên Cao"
                        PriorityLevel.TB -> "Ưu tiên Vừa"
                        PriorityLevel.THAP -> "Ưu tiên Thấp"
                    },
                    OnErrorContainer,
                    ErrorContainer
                )
                Badge(Icons.Default.NotificationsActive, "Nhắc nhở: Trước ${task.reminder}p", OnSurfaceVariant, SurfaceContainerLow)
            }
            Text(task.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLow)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Schedule, null, tint = Primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Hạn chót: ", fontSize = 12.sp, color = OnSurfaceVariant)
                Text("${task.dueTime} • ${task.dueDate}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Text("MÔ TẢ DỰ ÁN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
            Text(
                task.description ?: "Chưa có mô tả chi tiết.",
                fontSize = 13.sp,
                color = OnSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface)
                    .padding(12.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            images.take(2).forEach { img ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { previewUrl = img.url }
                ) {
                    AsyncImage(
                        model = img.url,
                        contentDescription = img.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xD90B1C30))))
                            .padding(8.dp)
                    ) {
                        Text(img.caption, color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Mục con cần làm", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(
                    "$completed/$total hoàn tất",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryFixed)
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)),
                color = Primary,
                trackColor = SurfaceContainerHigh
            )
            task.subtasks.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerLow)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.completed,
                        onCheckedChange = {
                            val updated = task.subtasks.toMutableList()
                            updated[index] = item.copy(completed = it)
                            val allDone = updated.isNotEmpty() && updated.all { s -> s.completed }
                            onUpdate(
                                task.copy(
                                    subtasks = updated,
                                    status = if (allDone) TaskStatus.DONE else task.status
                                )
                            )
                        },
                        colors = CheckboxDefaults.colors(checkedColor = Primary)
                    )
                    Column {
                        Text(
                            item.title,
                            fontSize = 13.sp,
                            fontWeight = if (item.completed) FontWeight.Normal else FontWeight.Medium,
                            textDecoration = if (item.completed) TextDecoration.LineThrough else null,
                            color = if (item.completed) Outline else OnSurface
                        )
                        Text(
                            if (item.completed) "Đã hoàn thành" else "Chờ xử lý",
                            fontSize = 11.sp,
                            color = if (item.completed) Tertiary else OnSurfaceVariant
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainer)
                    .clickable { showAddSub = true }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AddCircle, null, tint = Primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Thêm mục con", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (task.status == TaskStatus.DONE) SurfaceContainerLow else Tertiary)
                    .clickable {
                        onUpdate(
                            task.copy(
                                status = if (task.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE,
                                completedAt = if (task.status == TaskStatus.DONE) null else "17:00"
                            )
                        )
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = if (task.status == TaskStatus.DONE) OnSurfaceVariant else Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (task.status == TaskStatus.DONE) "Chuyển về Chưa xong" else "Đánh dấu Hoàn thành",
                    color = if (task.status == TaskStatus.DONE) OnSurfaceVariant else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainer)
                    .clickable { showEdit = true }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Tune, null, tint = Primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Tùy chỉnh", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Xóa công việc này?") },
            text = { Text("Bạn có chắc muốn xóa vĩnh viễn nhiệm vụ \"${task.title}\"? Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(onClick = {
                    showDelete = false
                    onDelete(task.id)
                    onBack()
                }) { Text("Xác nhận Xóa", color = Error) }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Hủy bỏ") }
            }
        )
    }

    if (showEdit) {
        AlertDialog(
            onDismissRequest = { showEdit = false },
            title = { Text("Chỉnh sửa nhanh") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Tiêu đề nhiệm vụ") },
                        singleLine = true
                    )
                    Text("Mức độ ưu tiên", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(PriorityLevel.THAP to "Thấp", PriorityLevel.TB to "TB", PriorityLevel.CAO to "Cao").forEach { (p, label) ->
                            Text(
                                label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (editPriority == p) Color.White else OnSurfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            editPriority != p -> SurfaceContainerLow
                                            p == PriorityLevel.CAO -> Error
                                            p == PriorityLevel.TB -> Secondary
                                            else -> Tertiary
                                        }
                                    )
                                    .clickable { editPriority = p }
                                    .padding(vertical = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                    Text("Gia hạn thời gian", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("+1 Giờ", "+1 Ngày", "Tuần tới").forEach { label ->
                            Text(
                                label,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceContainerLow)
                                    .clickable {
                                        onUpdate(task.copy(isOverdue = false, dueTime = "Gia hạn: $label", dueDate = "Đã gia hạn", title = editTitle.trim().ifEmpty { task.title }, priority = editPriority))
                                        showEdit = false
                                    }
                                    .padding(vertical = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editTitle.isNotBlank()) {
                        onUpdate(task.copy(title = editTitle.trim(), priority = editPriority))
                        showEdit = false
                    }
                }) { Text("Lưu thay đổi") }
            },
            dismissButton = {
                TextButton(onClick = { showEdit = false }) { Text("Hủy") }
            }
        )
    }

    if (showAddSub) {
        AlertDialog(
            onDismissRequest = { showAddSub = false },
            title = { Text("Thêm mục con") },
            text = {
                OutlinedTextField(
                    value = newSubTitle,
                    onValueChange = { newSubTitle = it },
                    placeholder = { Text("Tiêu đề mục con...") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newSubTitle.isNotBlank()) {
                        onUpdate(
                            task.copy(
                                subtasks = task.subtasks + Subtask("sub-${System.currentTimeMillis()}", newSubTitle.trim())
                            )
                        )
                        newSubTitle = ""
                        showAddSub = false
                    }
                }) { Text("Thêm") }
            },
            dismissButton = {
                TextButton(onClick = { showAddSub = false }) { Text("Hủy") }
            }
        )
    }

    previewUrl?.let { url ->
        Dialog(
            onDismissRequest = { previewUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xE60B1C30))
                    .clickable { previewUrl = null },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    IconButton(onClick = { previewUrl = null }) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    selected: Boolean,
    modifier: Modifier,
    showDot: Boolean = false,
    iconTint: Color = OnSurfaceVariant,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showDot) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Amber))
            Spacer(Modifier.width(4.dp))
        } else if (icon != null) {
            Icon(icon, null, tint = if (selected) iconTint else OnSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun Badge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color,
    bg: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}
