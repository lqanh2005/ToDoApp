package com.lqanh.todoandroid.data

// Trạng thái task: chưa làm, đang làm, đã xong.
enum class TaskStatus { TODO, DOING, DONE }

// Mức ưu tiên: cao, trung bình, thấp.
enum class PriorityLevel { CAO, TB, THAP }

// Các tab ở bottom navigation: Công việc và Lịch trình.
enum class NavigationTab { CONG_VIEC, LICH_TRINH }

// Việc con bên trong một task.
data class Subtask(
    val id: String,
    val title: String,
    val completed: Boolean = false
)

// Ảnh đính kèm của task (đường dẫn + chú thích).
data class TaskImage(
    val url: String,
    val caption: String
)

// Dữ liệu đầy đủ của một công việc (tiêu đề, trạng thái, danh mục, thời gian, nhắc nhở, việc con...).
data class Task(
    val id: String,
    val code: String,
    val sprint: String? = null,
    val title: String,
    val description: String? = null,
    val status: TaskStatus = TaskStatus.TODO,
    val priority: PriorityLevel = PriorityLevel.TB,
    val category: String = "work",
    val categoryLabel: String = "Work",
    val categoryEmoji: String? = "💼",
    val dueDate: String = "",
    val dueTime: String = "",
    val dueAtMillis: Long? = null,
    val startAtMillis: Long? = null,
    val endAtMillis: Long? = null,
    val awaitingCompletion: Boolean = false,
    val isOverdue: Boolean = false,
    val overdueText: String? = null,
    val subtasks: List<Subtask> = emptyList(),
    val attachmentsCount: Int? = null,
    val notes: String? = null,
    val location: String? = null,
    val reminder: String = "15",
    val progress: Int? = null,
    val images: List<TaskImage>? = null,
    val spec: String? = null,
    val completedAt: String? = null
)
