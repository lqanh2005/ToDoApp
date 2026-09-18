package com.lqanh.todoandroid.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lqanh.todoandroid.data.TaskRepository
import com.lqanh.todoandroid.data.TaskStatus

class TaskActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val taskId = intent?.getStringExtra(TaskNotifier.EXTRA_TASK_ID) ?: return
        val repo = TaskRepository(context)
        val tasks = repo.loadTasks().map { task ->
            if (task.id != taskId) task
            else when (intent.action) {
                TaskNotifier.ACTION_MARK_DONE -> task.copy(
                    status = TaskStatus.DONE,
                    awaitingCompletion = false,
                    isOverdue = false,
                    completedAt = "now"
                )
                TaskNotifier.ACTION_MARK_NOT_DONE -> task.copy(
                    status = TaskStatus.TODO,
                    awaitingCompletion = false,
                    isOverdue = true
                )
                else -> task
            }
        }
        repo.saveTasks(tasks)
        TaskNotifier.cancel(context, taskId)
        TaskAlarmScheduler.cancelForTask(context, taskId)
        context.sendBroadcast(
            Intent(TaskAlarmReceiver.ACTION_TASKS_CHANGED).setPackage(context.packageName)
        )
    }
}
