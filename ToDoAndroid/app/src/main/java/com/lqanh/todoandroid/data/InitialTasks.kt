package com.lqanh.todoandroid.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Danh sách task mẫu, được nạp vào database ở lần chạy đầu tiên.
// Ngày giờ tính theo thời điểm hiện tại để luôn có đủ các nhóm Previous / Today / Future / Complete.
object InitialTasks {
    val list: List<Task>
        get() {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return listOf(
                sample(
                    id = "task-1",
                    code = "#001",
                    title = "Check game",
                    description = "Play-test the latest build and note any bugs.",
                    category = "personal",
                    categoryLabel = "Personal",
                    dayOffset = -1,
                    hour = 8
                ),
                sample(
                    id = "task-2",
                    code = "#002",
                    title = "Finish presentation",
                    description = "Polish the slides and rehearse the talk.",
                    status = TaskStatus.DOING,
                    priority = PriorityLevel.CAO,
                    category = "work",
                    categoryLabel = "Work",
                    dayOffset = 0,
                    hour = currentHour
                ),
                sample(
                    id = "task-3",
                    code = "#003",
                    title = "Finish private game project",
                    description = "Wrap up the remaining levels and export the build.",
                    category = "personal",
                    categoryLabel = "Personal",
                    dayOffset = 1,
                    hour = 8
                ),
                sample(
                    id = "task-4",
                    code = "#004",
                    title = "Buy office supplies",
                    description = "A4 paper and whiteboard markers for meeting room 2.",
                    status = TaskStatus.DONE,
                    priority = PriorityLevel.THAP,
                    category = "shopping",
                    categoryLabel = "Shopping",
                    dayOffset = 0,
                    hour = 11,
                    completedAt = "now",
                    subtasks = listOf(Subtask("sub-4-1", "Pay the invoice", true))
                )
            )
        }

    // Tạo một task mẫu bắt đầu lúc hour:00 của ngày (hôm nay + dayOffset), kéo dài 1 giờ.
    private fun sample(
        id: String,
        code: String,
        title: String,
        description: String,
        category: String,
        categoryLabel: String,
        dayOffset: Int,
        hour: Int,
        status: TaskStatus = TaskStatus.TODO,
        priority: PriorityLevel = PriorityLevel.TB,
        completedAt: String? = null,
        subtasks: List<Subtask> = emptyList()
    ): Task {
        val day = Calendar.getInstance().apply {
            timeInMillis = TaskDateUtils.startOfDay()
            add(Calendar.DAY_OF_YEAR, dayOffset)
        }.timeInMillis
        val start = Calendar.getInstance().apply {
            timeInMillis = day
            set(Calendar.HOUR_OF_DAY, hour)
        }.timeInMillis
        val label = "%02d:00 · %s".format(hour, dayLabel(dayOffset, day))
        return Task(
            id = id,
            code = code,
            title = title,
            description = description,
            status = status,
            priority = priority,
            category = category,
            categoryLabel = categoryLabel,
            dueDate = label,
            dueTime = label,
            dueAtMillis = day,
            startAtMillis = start,
            endAtMillis = start + TaskDateUtils.DEFAULT_DURATION_MS,
            reminder = "5",
            completedAt = completedAt,
            subtasks = subtasks
        )
    }

    // Nhãn ngày giống khi người dùng tự chọn: "today", "Tomorrow" hoặc "MMM d".
    private fun dayLabel(dayOffset: Int, dayMillis: Long): String = when (dayOffset) {
        0 -> "today"
        1 -> "Tomorrow"
        else -> SimpleDateFormat("MMM d", Locale.ENGLISH).format(Date(dayMillis))
    }
}
