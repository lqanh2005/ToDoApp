package com.lqanh.todoandroid.data

enum class TaskStatus { TODO, DOING, DONE }

enum class PriorityLevel { CAO, TB, THAP }

enum class NavigationTab { CONG_VIEC, LICH_TRINH }

data class Subtask(
    val id: String,
    val title: String,
    val completed: Boolean = false
)

data class TaskImage(
    val url: String,
    val caption: String
)

data class Task(
    val id: String,
    val code: String,
    val sprint: String? = null,
    val title: String,
    val description: String? = null,
    val status: TaskStatus = TaskStatus.TODO,
    val priority: PriorityLevel = PriorityLevel.TB,
    val category: String = "work",
    val categoryLabel: String = "Công việc",
    val categoryEmoji: String? = "💼",
    val dueDate: String = "",
    val dueTime: String = "",
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
