package com.lqanh.todoandroid.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lqanh.todoandroid.data.Task
import com.lqanh.todoandroid.data.TaskStatus
import com.lqanh.todoandroid.ui.theme.Error
import com.lqanh.todoandroid.ui.theme.ErrorContainer
import com.lqanh.todoandroid.ui.theme.OnErrorContainer
import com.lqanh.todoandroid.ui.theme.OnSurface
import com.lqanh.todoandroid.ui.theme.OnSurfaceVariant
import com.lqanh.todoandroid.ui.theme.Outline
import com.lqanh.todoandroid.ui.theme.Primary
import com.lqanh.todoandroid.ui.theme.PrimaryFixed
import com.lqanh.todoandroid.ui.theme.Secondary
import com.lqanh.todoandroid.ui.theme.SecondaryFixed
import com.lqanh.todoandroid.ui.theme.SurfaceContainer
import com.lqanh.todoandroid.ui.theme.SurfaceContainerLow
import com.lqanh.todoandroid.ui.theme.Tertiary
import com.lqanh.todoandroid.ui.theme.TertiaryFixed

@Composable
fun ScheduleScreen(
    tasks: List<Task>,
    onSelectTask: (Task) -> Unit,
    onChangeStatus: (String, TaskStatus) -> Unit
) {
    var viewMode by remember { mutableStateOf(0) }
    var selectedDay by remember { mutableIntStateOf(25) }

    val todoTasks = tasks.filter { it.status == TaskStatus.TODO }
    val doingTasks = tasks.filter { it.status == TaskStatus.DOING }
    val doneTasks = tasks.filter { it.status == TaskStatus.DONE }
    val overdueCount = tasks.count { it.isOverdue && it.status != TaskStatus.DONE }
    val percent = if (tasks.isEmpty()) 0 else ((doneTasks.size * 100f) / tasks.size).toInt()

    val days = listOf(
        Triple("T2", 23, 2),
        Triple("T3", 24, 1),
        Triple("T4", 25, 3),
        Triple("T5", 26, 2),
        Triple("T6", 27, 3),
        Triple("T7", 28, 1),
        Triple("CN", 29, 1)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 100.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CalendarToday, null, tint = Primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Năm 2025", fontSize = 11.sp, color = OnSurfaceVariant)
                Text("Tháng 6, 2025", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceContainer)
                    .padding(4.dp)
            ) {
                listOf("Ngày", "Tuần", "Tháng").forEachIndexed { index, label ->
                    Text(
                        label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (viewMode == index) Color.White else OnSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (viewMode == index) Primary else Color.Transparent)
                            .clickable { viewMode = index }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            days.forEach { (name, num, dots) ->
                val selected = selectedDay == num
                Column(
                    modifier = Modifier
                        .width(if (selected) 54.dp else 50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) Primary else SurfaceContainerLow)
                        .clickable { selectedDay = num }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(name, fontSize = 11.sp, color = if (selected) PrimaryFixed else OnSurfaceVariant)
                    Text("$num", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (selected) Color.White else OnSurface)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(dots) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) TertiaryFixed else Color(0xFFC3C6D7))
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Primary))
                Spacer(Modifier.width(8.dp))
                Text("Tiến độ hôm nay", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.weight(1f))
                Text(
                    "Tuần 26",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryFixed)
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(96.dp)) {
                        val stroke = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                        drawArc(Color(0xFFE5EEFF), 0f, 360f, false, style = stroke)
                        drawArc(Primary, -90f, 360f * percent / 100f, false, style = stroke)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$percent%", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Xong", fontSize = 11.sp, color = OnSurfaceVariant)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStat("Đã xong", "${doneTasks.size}", Tertiary, Modifier.weight(1f))
                    MiniStat("Quá hạn", "$overdueCount", Error, Modifier.weight(1f))
                    MiniStat("Sắp tới", "${todoTasks.size}", Primary, Modifier.weight(1f))
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ViewColumn, null, tint = Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text("Bảng trạng thái", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("Vuốt ngang để xem", fontSize = 11.sp, color = OnSurfaceVariant)
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KanbanColumn("Cần làm", todoTasks.size, Outline) {
                todoTasks.take(3).forEach { task ->
                    KanbanCard(task.title, task.categoryLabel, task.dueTime.split(" ").firstOrNull() ?: "") {
                        onSelectTask(task)
                    }
                    Text(
                        "→ Đang làm",
                        fontSize = 11.sp,
                        color = Primary,
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .clickable { onChangeStatus(task.id, TaskStatus.DOING) }
                    )
                }
            }
            KanbanColumn("Đang làm", doingTasks.size, Secondary) {
                doingTasks.forEach { task ->
                    val prog = task.progress
                        ?: if (task.subtasks.isNotEmpty()) {
                            ((task.subtasks.count { it.completed } * 100f) / task.subtasks.size).toInt()
                        } else 50
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .clickable { onSelectTask(task) }
                            .padding(12.dp)
                    ) {
                        Row {
                            Text(task.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.HourglassTop, null, tint = Secondary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { prog / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                            color = Secondary,
                            trackColor = SurfaceContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Row {
                            Text(task.notes ?: "Tiến độ: $prog%", fontSize = 11.sp, color = OnSurfaceVariant, modifier = Modifier.weight(1f), maxLines = 1)
                            Text("Xong", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Tertiary, modifier = Modifier.clickable {
                                onChangeStatus(task.id, TaskStatus.DONE)
                            })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            KanbanColumn("Hoàn thành", doneTasks.size, Tertiary) {
                doneTasks.take(4).forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(0.7f))
                            .clickable { onSelectTask(task) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            task.title,
                            fontSize = 12.sp,
                            color = OnSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.CheckCircle, null, tint = Tertiary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Timeline, null, tint = Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text("Lịch trình trong ngày", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        TimelineItem("Họp Daily Scrum", "09:00", "Cập nhật tiến độ dự án với đội kỹ thuật", done = true)
        TimelineItem("Mua vật tư văn phòng", "11:00", "Giấy in A4 và bút dạ bảng trắng", done = true)
        TimelineItem(
            "Nộp báo cáo quý 2",
            "14:00",
            "Báo cáo tài chính và tăng trưởng người dùng",
            overdue = true,
            action = "Giải quyết ngay",
            onAction = { tasks.find { it.title.contains("báo cáo", true) }?.let(onSelectTask) }
        )
        TimelineItem("Học tiếng Anh bài 12", "16:30", "Ngữ pháp thì hoàn thành & Luyện từ vựng", doing = true)
        TimelineItem("Đi siêu thị gia đình", "19:00", "Thực phẩm tươi sống và sữa chua cho tuần mới")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceContainerLow)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(PrimaryFixed),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, null, tint = Primary)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Bạn đang làm rất tốt!", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("Đã đạt $percent% mục tiêu trong ngày. Chỉ còn 2 việc trước bữa tối.", fontSize = 12.sp, color = OnSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLow)
            .padding(8.dp)
    ) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = color)
        Text(label, fontSize = 11.sp, color = OnSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun KanbanColumn(title: String, count: Int, dot: Color, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLow)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(dot))
            Spacer(Modifier.width(6.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(
                "$count",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun KanbanCard(title: String, category: String, time: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(8.dp))
        Row {
            Text(
                category,
                fontSize = 11.sp,
                color = OnSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
            Spacer(Modifier.weight(1f))
            Text(time, fontSize = 11.sp, color = OnSurfaceVariant)
        }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun TimelineItem(
    title: String,
    time: String,
    desc: String,
    done: Boolean = false,
    overdue: Boolean = false,
    doing: Boolean = false,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (overdue) ErrorContainer.copy(0.3f) else Color.White)
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    when {
                        done -> TertiaryFixed.copy(0.5f)
                        overdue -> Error
                        doing -> Secondary
                        else -> SurfaceContainer
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    done -> Icons.Default.Check
                    overdue -> Icons.Default.PriorityHigh
                    doing -> Icons.Default.PlayArrow
                    else -> Icons.Default.RadioButtonUnchecked
                },
                contentDescription = null,
                tint = when {
                    done -> Color(0xFF002113)
                    overdue || doing -> Color.White
                    else -> OnSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (overdue) OnErrorContainer else if (done) OnSurfaceVariant else OnSurface,
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    time,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        overdue -> Error
                        doing -> Secondary
                        done -> Tertiary
                        else -> OnSurfaceVariant
                    }
                )
            }
            Text(desc, fontSize = 12.sp, color = OnSurfaceVariant)
            if (action != null && onAction != null) {
                Spacer(Modifier.height(6.dp))
                Text(action, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Error, modifier = Modifier.clickable(onClick = onAction))
            }
        }
    }
}
