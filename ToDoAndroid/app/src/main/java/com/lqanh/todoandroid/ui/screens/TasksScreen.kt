package com.lqanh.todoandroid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lqanh.todoandroid.data.PriorityLevel
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
import com.lqanh.todoandroid.ui.theme.PrimaryContainer
import com.lqanh.todoandroid.ui.theme.PrimaryFixed
import com.lqanh.todoandroid.ui.theme.SecondaryFixed
import com.lqanh.todoandroid.ui.theme.SurfaceContainer
import com.lqanh.todoandroid.ui.theme.SurfaceContainerHigh
import com.lqanh.todoandroid.ui.theme.SurfaceContainerHighest
import com.lqanh.todoandroid.ui.theme.SurfaceContainerLow
import com.lqanh.todoandroid.ui.theme.Tertiary
import com.lqanh.todoandroid.ui.theme.TertiaryFixed

@Composable
fun TasksScreen(
    tasks: List<Task>,
    searchQuery: String,
    isSearchOpen: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit,
    onSelectTask: (Task) -> Unit,
    onToggleComplete: (String) -> Unit,
    onChangeStatus: (String, TaskStatus) -> Unit,
    onDeleteTask: (String) -> Unit,
    onOpenCreate: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("all") }
    var selectedStatus by remember { mutableStateOf(TaskStatus.TODO) }
    var sortMode by remember { mutableStateOf(0) }
    var showAiModal by remember { mutableStateOf(false) }
    var menuTaskId by remember { mutableStateOf<String?>(null) }

    val doneCount = tasks.count { it.status == TaskStatus.DONE }
    val todoCount = tasks.count { it.status == TaskStatus.TODO }
    val doingCount = tasks.count { it.status == TaskStatus.DOING }
    val progress = if (tasks.isEmpty()) 0 else ((doneCount * 100f) / tasks.size).toInt()

    val categoryCounts = mapOf(
        "all" to tasks.size,
        "work" to tasks.count { it.category == "work" },
        "personal" to tasks.count { it.category == "personal" },
        "study" to tasks.count { it.category == "study" },
        "shopping" to tasks.count { it.category == "shopping" }
    )

    val displayed = tasks
        .filter { it.status == selectedStatus }
        .filter { selectedCategory == "all" || it.category == selectedCategory }
        .filter {
            if (searchQuery.isBlank()) true
            else {
                val q = searchQuery.lowercase()
                it.title.lowercase().contains(q) ||
                    (it.description?.lowercase()?.contains(q) == true) ||
                    it.categoryLabel.lowercase().contains(q)
            }
        }
        .sortedWith { a, b ->
            when (sortMode) {
                0 -> priorityScore(b.priority) - priorityScore(a.priority)
                1 -> a.dueTime.compareTo(b.dueTime)
                else -> a.title.compareTo(b.title)
            }
        }

    val sortLabel = when (sortMode) {
        0 -> "Ưu tiên cao"
        1 -> "Theo giờ hẹn"
        else -> "Tên việc A-Z"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 100.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isSearchOpen) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tìm kiếm theo tên công việc, ghi chú...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Primary) },
                trailingIcon = {
                    TextButton(onClick = onCloseSearch) { Text("Đóng", color = Primary) }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceContainerLow,
                    unfocusedContainerColor = SurfaceContainerLow,
                    focusedBorderColor = PrimaryFixed,
                    unfocusedBorderColor = PrimaryFixed
                ),
                singleLine = true
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceContainerLow)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚡ Tiến độ hôm nay", fontWeight = FontWeight.SemiBold, color = OnSurface, fontSize = 14.sp)
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PrimaryContainer)
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text("$progress%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Bạn đã hoàn thành $doneCount/${tasks.size} nhiệm vụ. Cố gắng thêm chút nữa nhé!",
                fontSize = 12.sp,
                color = OnSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = PrimaryContainer,
                trackColor = SurfaceContainerHighest
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("all", "Tất cả", null),
                Triple("work", "Công việc", "💼"),
                Triple("personal", "Cá nhân", "👤"),
                Triple("study", "Học tập", "📚"),
                Triple("shopping", "Mua sắm", "🛒")
            ).forEach { (id, label, emoji) ->
                val selected = selectedCategory == id
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Primary else SurfaceContainer)
                        .clickable { selectedCategory = id }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (emoji != null) Text("$emoji ", fontSize = 12.sp)
                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (selected) Color.White else OnSurfaceVariant)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${categoryCounts[id] ?: 0}",
                        fontSize = 11.sp,
                        color = if (selected) Color.White else OnSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) Color.White.copy(0.2f) else SurfaceContainerHighest)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceContainerHigh.copy(alpha = 0.6f))
                .padding(4.dp)
        ) {
            StatusTab("Cần làm", todoCount, selectedStatus == TaskStatus.TODO, Modifier.weight(1f)) {
                selectedStatus = TaskStatus.TODO
            }
            StatusTab("Đang làm", doingCount, selectedStatus == TaskStatus.DOING, Modifier.weight(1f)) {
                selectedStatus = TaskStatus.DOING
            }
            StatusTab("Xong", doneCount, selectedStatus == TaskStatus.DONE, Modifier.weight(1f)) {
                selectedStatus = TaskStatus.DONE
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                when (selectedStatus) {
                    TaskStatus.TODO -> "Danh sách cần làm"
                    TaskStatus.DOING -> "Đang thực hiện"
                    TaskStatus.DONE -> "Đã hoàn thành"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnSurface
            )
            Spacer(Modifier.width(6.dp))
            Text("(${displayed.size} việc)", fontSize = 11.sp, color = OnSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { sortMode = (sortMode + 1) % 3 }
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.SwapVert, null, tint = Primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(2.dp))
                Text(sortLabel, fontSize = 12.sp, color = Primary, fontWeight = FontWeight.Medium)
            }
        }

        if (displayed.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.TaskAlt, null, tint = Outline, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(8.dp))
                Text("Không có công việc nào trong mục này", fontWeight = FontWeight.Medium, color = OnSurface)
                Text("Bạn có thể tạo công việc mới bất cứ lúc nào", fontSize = 12.sp, color = OnSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Text(
                    "+ Thêm công việc ngay",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary)
                        .clickable(onClick = onOpenCreate)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        } else {
            displayed.forEach { task ->
                TaskCard(
                    task = task,
                    menuExpanded = menuTaskId == task.id,
                    onMenuToggle = { menuTaskId = if (menuTaskId == task.id) null else task.id },
                    onDismissMenu = { menuTaskId = null },
                    onSelect = { onSelectTask(task) },
                    onToggleComplete = { onToggleComplete(task.id) },
                    onChangeStatus = { onChangeStatus(task.id, it) },
                    onDelete = { onDeleteTask(task.id); menuTaskId = null }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceContainerHigh.copy(0.4f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SecondaryFixed),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Psychology, null, tint = Color(0xFF23005C))
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Gợi ý từ trợ lý AI", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
                Text("Nên giải quyết slide quý 2 trước 16:00 để tránh trễ hạn.", fontSize = 12.sp, color = OnSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(
                "Xem",
                color = Primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { showAiModal = true }.padding(8.dp)
            )
        }
    }

    if (showAiModal) {
        AlertDialog(
            onDismissRequest = { showAiModal = false },
            title = { Text("Phân tích ToDoList AI") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Ưu tiên cao nhất", fontWeight = FontWeight.SemiBold, color = Primary)
                    Text("Công việc \"Hoàn thiện slide báo cáo quý 2\" đã trễ 2 giờ. Hãy dành 45 phút tiếp theo để hoàn thành 2 mục con còn lại.")
                    Text("Đề xuất phân bổ lịch", fontWeight = FontWeight.SemiBold, color = Tertiary)
                    Text("Lịch trình 17:00 nộp báo cáo tài chính sẽ diễn ra suôn sẻ nếu bạn duyệt xong slide trước 16:00.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showAiModal = false }) {
                    Text("Đã hiểu & Bắt đầu ngay")
                }
            }
        )
    }
}

@Composable
private fun StatusTab(label: String, count: Int, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
        Spacer(Modifier.width(4.dp))
        Text(
            "$count",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryFixed)
                .padding(horizontal = 6.dp, vertical = 1.dp)
        )
    }
}

@Composable
private fun TaskCard(
    task: Task,
    menuExpanded: Boolean,
    onMenuToggle: () -> Unit,
    onDismissMenu: () -> Unit,
    onSelect: () -> Unit,
    onToggleComplete: () -> Unit,
    onChangeStatus: (TaskStatus) -> Unit,
    onDelete: () -> Unit
) {
    val isDone = task.status == TaskStatus.DONE
    val isDoing = task.status == TaskStatus.DOING
    val borderColor = when {
        task.isOverdue -> Error
        task.priority == PriorityLevel.CAO -> Error
        task.priority == PriorityLevel.TB -> Amber
        else -> Tertiary
    }
    val completedSubs = task.subtasks.count { it.completed }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDone) Color.White.copy(0.8f) else Color.White)
            .clickable(onClick = onSelect)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(6.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                .background(borderColor)
        )
        Row(modifier = Modifier.padding(start = 10.dp)) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isDone) Primary else SurfaceContainerHigh)
                    .clickable { onToggleComplete() },
                contentAlignment = Alignment.Center
            ) {
                if (isDone) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (task.isOverdue && !isDone) {
                        Text(
                            task.overdueText ?: "Quá hạn",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnErrorContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ErrorContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        when (task.priority) {
                            PriorityLevel.CAO -> "Ưu tiên Cao"
                            PriorityLevel.TB -> "Ưu tiên Trung bình"
                            PriorityLevel.THAP -> "Ưu tiên Thấp"
                        },
                        fontSize = 11.sp,
                        color = when (task.priority) {
                            PriorityLevel.CAO -> Error
                            PriorityLevel.TB -> Color(0xFF78350F)
                            PriorityLevel.THAP -> Color(0xFF005236)
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (task.priority) {
                                    PriorityLevel.CAO -> Error.copy(0.1f)
                                    PriorityLevel.TB -> Color(0xFFFEF3C7)
                                    PriorityLevel.THAP -> TertiaryFixed.copy(0.4f)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                    Text(
                        "${task.categoryEmoji ?: ""} ${task.categoryLabel}",
                        fontSize = 11.sp,
                        color = OnSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDone) Outline else OnSurface,
                    textDecoration = if (isDone) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    buildString {
                        append(task.dueTime)
                        if (task.subtasks.isNotEmpty()) append(" · $completedSubs/${task.subtasks.size} mục")
                        task.location?.let { append(" · $it") }
                    },
                    fontSize = 12.sp,
                    color = if (task.isOverdue && !isDone) Error else Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column {
                IconButton(onClick = {
                    onChangeStatus(if (isDoing) TaskStatus.DONE else TaskStatus.DOING)
                }) {
                    Icon(
                        if (isDoing) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = if (isDoing) Color(0xFFB45309) else Primary
                    )
                }
                Box {
                    IconButton(onClick = onMenuToggle) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = onDismissMenu) {
                        DropdownMenuItem(
                            text = { Text("Xem chi tiết") },
                            onClick = { onDismissMenu(); onSelect() },
                            leadingIcon = { Icon(Icons.Default.Visibility, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Đang làm") },
                            onClick = { onChangeStatus(TaskStatus.DOING); onDismissMenu() },
                            leadingIcon = { Icon(Icons.Default.PlayArrow, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Hoàn thành") },
                            onClick = { onChangeStatus(TaskStatus.DONE); onDismissMenu() },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Xóa nhiệm vụ", color = Error) },
                            onClick = onDelete,
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = Error) }
                        )
                    }
                }
            }
        }
    }
}

private fun priorityScore(p: PriorityLevel) = when (p) {
    PriorityLevel.CAO -> 3
    PriorityLevel.TB -> 2
    PriorityLevel.THAP -> 1
}
