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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lqanh.todoandroid.data.PriorityLevel
import com.lqanh.todoandroid.data.Subtask
import com.lqanh.todoandroid.data.Task
import com.lqanh.todoandroid.data.TaskStatus
import com.lqanh.todoandroid.ui.theme.Error
import com.lqanh.todoandroid.ui.theme.ErrorContainer
import com.lqanh.todoandroid.ui.theme.OnErrorContainer
import com.lqanh.todoandroid.ui.theme.OnSurface
import com.lqanh.todoandroid.ui.theme.OnSurfaceVariant
import com.lqanh.todoandroid.ui.theme.Outline
import com.lqanh.todoandroid.ui.theme.Primary
import com.lqanh.todoandroid.ui.theme.PrimaryContainer
import com.lqanh.todoandroid.ui.theme.PrimaryFixed
import com.lqanh.todoandroid.ui.theme.Secondary
import com.lqanh.todoandroid.ui.theme.SecondaryFixed
import com.lqanh.todoandroid.ui.theme.SurfaceContainer
import com.lqanh.todoandroid.ui.theme.SurfaceContainerLow
import com.lqanh.todoandroid.ui.theme.Tertiary
import com.lqanh.todoandroid.ui.theme.TertiaryFixed
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    onClose: () -> Unit,
    onCreate: (Task) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(PriorityLevel.CAO) }
    var dueDateKey by remember { mutableStateOf("today") }
    var timeSlot by remember { mutableStateOf("14:30") }
    var showTimePicker by remember { mutableStateOf(false) }
    var reminderActive by remember { mutableStateOf(true) }
    var reminderMinutes by remember { mutableStateOf("15") }
    var category by remember { mutableStateOf("work") }
    var subtaskInput by remember { mutableStateOf("") }
    val subtasks = remember { mutableStateListOf<Subtask>() }
    var showNewCategory by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }
    val initialHour = timeSlot.substringBefore(":").toIntOrNull() ?: 14
    val initialMinute = timeSlot.substringAfter(":").toIntOrNull() ?: 30
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    val categories = remember {
        mutableStateListOf(
            Triple("work", "Công việc", "💼"),
            Triple("personal", "Cá nhân", "👤"),
            Triple("study", "Học tập", "📚"),
            Triple("shopping", "Mua sắm", "🛒")
        )
    }

    val canSave = title.trim().isNotEmpty()
    val priorityLabel = when (priority) {
        PriorityLevel.CAO -> "Quan trọng - Cần làm ngay" to Error
        PriorityLevel.TB -> "Bình thường - Tuần này" to Secondary
        PriorityLevel.THAP -> "Thảnh thơi - Khi có thời gian" to Tertiary
    }
    val dueLabel = when (dueDateKey) {
        "today" -> "Hôm nay, 25/06/2025"
        "tomorrow" -> "Ngày mai, 26/06/2025"
        "weekend" -> "Thứ Bảy, 28/06/2025"
        else -> "Đã chọn lịch"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng")
                }
                Text("Tạo công việc mới", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(
                    "Lưu",
                    color = if (canSave) Color.White else Outline,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (canSave) PrimaryContainer else SurfaceContainer)
                        .clickable(enabled = canSave) {
                            saveTask(
                                title, description, priority, dueLabel, timeSlot, dueDateKey,
                                reminderMinutes, category, categories, subtasks, onCreate
                            )
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimaryContainer.copy(0.1f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AutoAwesome, null, tint = Primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "ToDoList sẽ tự động phân phối lời nhắc phù hợp với thói quen của bạn.",
                    fontSize = 12.sp,
                    color = Primary
                )
            }

            FieldLabel("Tên công việc *")
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ví dụ: Họp review thiết kế giao diện UI...") },
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors(),
                singleLine = true
            )

            FieldLabel("Mô tả & Ghi chú chi tiết")
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                placeholder = { Text("Thêm ghi chú, checklist hoặc nội dung chi tiết...") },
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors()
            )

            Row {
                FieldLabel("Mức độ ưu tiên", Modifier.weight(1f))
                Text(priorityLabel.first, fontSize = 11.sp, color = priorityLabel.second, fontWeight = FontWeight.Medium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PriorityChip("Cao", PriorityLevel.CAO, priority, Error, ErrorContainer, Modifier.weight(1f)) { priority = it }
                PriorityChip("Vừa", PriorityLevel.TB, priority, Secondary, SecondaryFixed, Modifier.weight(1f)) { priority = it }
                PriorityChip("Thấp", PriorityLevel.THAP, priority, Tertiary, TertiaryFixed.copy(0.4f), Modifier.weight(1f)) { priority = it }
            }

            FieldLabel("Hạn hoàn thành")
            Text(dueLabel, fontSize = 11.sp, color = Primary, fontWeight = FontWeight.Medium)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("today" to "Hôm nay", "tomorrow" to "Ngày mai", "weekend" to "Cuối tuần").forEach { (key, label) ->
                    Chip(label, dueDateKey == key) { dueDateKey = key }
                }
            }

            FieldLabel("Thời gian thực hiện")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLow)
                    .clickable { showTimePicker = true }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryFixed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccessTime, null, tint = Primary, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Giờ đã chọn", fontSize = 11.sp, color = OnSurfaceVariant)
                    Text(timeSlot, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                }
                Text(
                    "Chọn giờ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Primary)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            if (showTimePicker) {
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                timeSlot = "%02d:%02d".format(timePickerState.hour, timePickerState.minute)
                                showTimePicker = false
                            }
                        ) { Text("Xác nhận", color = Primary, fontWeight = FontWeight.Bold) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Hủy", color = OnSurfaceVariant)
                        }
                    },
                    title = {
                        Text("Chọn thời gian thực hiện", fontWeight = FontWeight.Bold, color = OnSurface)
                    },
                    text = {
                        TimePicker(
                            state = timePickerState,
                            colors = TimePickerDefaults.colors(
                                clockDialColor = SurfaceContainerLow,
                                selectorColor = Primary,
                                timeSelectorSelectedContainerColor = Primary,
                                timeSelectorSelectedContentColor = Color.White,
                                timeSelectorUnselectedContainerColor = SurfaceContainer,
                                timeSelectorUnselectedContentColor = OnSurface
                            )
                        )
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLow)
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(SurfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.NotificationsActive, null, tint = Primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Nhắc nhở thông báo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Gửi báo động trên màn hình khóa", fontSize = 11.sp, color = Outline)
                    }
                    Switch(
                        checked = reminderActive,
                        onCheckedChange = { reminderActive = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = Primary)
                    )
                }
                if (reminderActive) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("15" to "15 phút", "30" to "30 phút", "60" to "1 giờ", "0" to "Đúng giờ").forEach { (v, label) ->
                            Chip(label, reminderMinutes == v) { reminderMinutes = v }
                        }
                    }
                }
            }

            FieldLabel("Danh mục công việc")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (id, label, emoji) ->
                    Chip("$emoji $label", category == id) { category = id }
                }
                Chip("+ Mới", false) { showNewCategory = true }
            }

            Row {
                FieldLabel("Việc cần làm con (Sub-tasks)", Modifier.weight(1f))
                Text("${subtasks.size} việc", fontSize = 11.sp, color = Outline)
            }
            subtasks.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLow)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (item.completed) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                        null,
                        tint = Primary,
                        modifier = Modifier.clickable {
                            subtasks[index] = item.copy(completed = !item.completed)
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        item.title,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f),
                        textDecoration = if (item.completed) TextDecoration.LineThrough else null,
                        color = if (item.completed) Outline else OnSurface
                    )
                    Icon(Icons.Default.Close, null, tint = Outline, modifier = Modifier
                        .size(18.dp)
                        .clickable { subtasks.removeAt(index) })
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = subtaskInput,
                    onValueChange = { subtaskInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Thêm một đầu việc nhỏ...") },
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors(),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PrimaryContainer)
                        .clickable {
                            if (subtaskInput.isNotBlank()) {
                                subtasks.add(Subtask("sub-${System.currentTimeMillis()}", subtaskInput.trim()))
                                subtaskInput = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (canSave) PrimaryContainer else SurfaceContainer)
                    .clickable(enabled = canSave) {
                        saveTask(
                            title, description, priority, dueLabel, timeSlot, dueDateKey,
                            reminderMinutes, category, categories, subtasks, onCreate
                        )
                    }
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AddTask, null, tint = if (canSave) Color.White else Outline)
                Spacer(Modifier.width(8.dp))
                Text("Tạo công việc ngay", color = if (canSave) Color.White else Outline, fontWeight = FontWeight.SemiBold)
            }
            Text(
                "Nhấn hoàn tất để tự động lưu vào bảng tiến độ hôm nay",
                fontSize = 12.sp,
                color = Outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showNewCategory) {
        AlertDialog(
            onDismissRequest = { showNewCategory = false },
            title = { Text("Thêm danh mục mới") },
            text = {
                OutlinedTextField(
                    value = newCatName,
                    onValueChange = { newCatName = it },
                    placeholder = { Text("Tên danh mục...") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCatName.isNotBlank()) {
                        val id = "cat-${System.currentTimeMillis()}"
                        categories.add(Triple(id, newCatName.trim(), "📌"))
                        category = id
                        newCatName = ""
                        showNewCategory = false
                    }
                }) { Text("Thêm danh mục") }
            },
            dismissButton = {
                TextButton(onClick = { showNewCategory = false }) { Text("Hủy") }
            }
        )
    }
}

@Composable
private fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceVariant, modifier = modifier)
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = SurfaceContainerLow,
    unfocusedContainerColor = SurfaceContainerLow,
    unfocusedBorderColor = Color.Transparent,
    focusedBorderColor = PrimaryContainer
)

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (selected) Color.White else OnSurface,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Primary else SurfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@Composable
private fun PriorityChip(
    label: String,
    value: PriorityLevel,
    selected: PriorityLevel,
    activeColor: Color,
    activeBg: Color,
    modifier: Modifier,
    onClick: (PriorityLevel) -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected == value) activeBg else SurfaceContainerLow)
            .clickable { onClick(value) }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(activeColor))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, fontWeight = if (selected == value) FontWeight.SemiBold else FontWeight.Normal, color = if (selected == value) activeColor else OnSurfaceVariant)
    }
}

private fun saveTask(
    title: String,
    description: String,
    priority: PriorityLevel,
    dueLabel: String,
    timeSlot: String,
    dueDateKey: String,
    reminderMinutes: String,
    category: String,
    categories: List<Triple<String, String, String>>,
    subtasks: List<Subtask>,
    onCreate: (Task) -> Unit
) {
    val cat = categories.find { it.first == category }
    onCreate(
        Task(
            id = "task-${System.currentTimeMillis()}",
            code = "#${100 + Random.nextInt(900)}",
            title = title.trim(),
            description = description.trim(),
            priority = priority,
            status = TaskStatus.TODO,
            category = category,
            categoryLabel = cat?.second ?: "Công việc",
            categoryEmoji = cat?.third ?: "📌",
            dueDate = dueLabel,
            dueTime = "$timeSlot (${if (dueDateKey == "today") "Hôm nay" else "Sắp tới"})",
            reminder = reminderMinutes,
            subtasks = subtasks.toList()
        )
    )
}
