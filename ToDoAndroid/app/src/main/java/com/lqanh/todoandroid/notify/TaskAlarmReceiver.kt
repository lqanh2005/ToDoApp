package com.lqanh.todoandroid.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lqanh.todoandroid.data.TaskRepository
import com.lqanh.todoandroid.data.TaskStatus

// Nhận báo thức đã đặt bởi TaskAlarmScheduler khi task đến giờ bắt đầu hoặc kết thúc.
class TaskAlarmReceiver : BroadcastReceiver() {
    // Đến giờ bắt đầu: chuyển task sang DOING. Đến giờ kết thúc: đánh dấu quá hạn, chờ xác nhận. Sau đó gửi thông báo và báo cho app tải lại.
    override fun onReceive(context: Context, intent: Intent?) {
        val taskId = intent?.getStringExtra(TaskAlarmScheduler.EXTRA_TASK_ID) ?: return
        val title = intent.getStringExtra(TaskAlarmScheduler.EXTRA_TASK_TITLE).orEmpty()
        val repo = TaskRepository(context)
        val task = repo.loadTasks().firstOrNull { it.id == taskId } ?: return

        when (intent.action) {
            TaskAlarmScheduler.ACTION_START -> {
                if (task.status == TaskStatus.TODO) {
                    repo.saveTask(task.copy(status = TaskStatus.DOING))
                    TaskNotifier.notifyTaskStarted(context, taskId, title.ifBlank { task.title })
                    context.sendBroadcast(
                        Intent(ACTION_TASKS_CHANGED).setPackage(context.packageName)
                    )
                }
            }
            TaskAlarmScheduler.ACTION_END -> {
                if (task.status != TaskStatus.DONE) {
                    repo.saveTask(
                        task.copy(
                            status = if (task.status == TaskStatus.TODO) TaskStatus.DOING else task.status,
                            awaitingCompletion = true,
                            isOverdue = true
                        )
                    )
                    TaskNotifier.notifyTaskEnded(context, taskId, title.ifBlank { task.title })
                    context.sendBroadcast(
                        Intent(ACTION_TASKS_CHANGED).setPackage(context.packageName)
                    )
                }
            }
        }
    }

    companion object {
        const val ACTION_TASKS_CHANGED = "com.lqanh.todoandroid.ACTION_TASKS_CHANGED"
    }
}
