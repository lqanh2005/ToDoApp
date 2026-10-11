package com.lqanh.todoandroid.data

import android.content.Context

// Lớp trung gian giữa ViewModel/Receiver và SQLite: mọi thao tác lưu trữ task đều đi qua đây.
class TaskRepository(context: Context) {
    private val dbHelper = TaskDatabaseHelper.getInstance(context)

    // Lấy toàn bộ task từ SQLite. Nếu đọc lỗi thì trả về dữ liệu mẫu để app vẫn hiển thị được.
    fun loadTasks(): List<Task> =
        runCatching { TaskDao.readAll(dbHelper.readableDatabase) }
            .getOrElse { InitialTasks.list }

    // Lưu lại toàn bộ danh sách task (ghi đè dữ liệu cũ, giữ nguyên thứ tự).
    fun saveTasks(tasks: List<Task>) {
        TaskDao.replaceAll(dbHelper.writableDatabase, tasks)
    }

    // Thêm mới hoặc cập nhật một task duy nhất mà không phải ghi lại cả danh sách.
    fun saveTask(task: Task) {
        TaskDao.upsert(dbHelper.writableDatabase, task)
    }

    // Xóa một task khỏi database.
    fun deleteTask(taskId: String) {
        TaskDao.delete(dbHelper.writableDatabase, taskId)
    }

    // Xóa hết dữ liệu hiện tại và nạp lại danh sách task mẫu.
    fun resetTasks(): List<Task> {
        val tasks = InitialTasks.list
        saveTasks(tasks)
        return tasks
    }
}
