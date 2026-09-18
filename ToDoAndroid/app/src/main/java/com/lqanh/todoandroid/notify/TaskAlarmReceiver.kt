package com.lqanh.todoandroid.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lqanh.todoandroid.data.TaskRepository
import com.lqanh.todoandroid.data.TaskStatus

class TaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val taskId = intent?.getStringExtra(TaskAlarmScheduler.EXTRA_TASK_ID) ?: return
        val title = intent.getStringExtra(TaskAlarmScheduler.EXTRA_TASK_TITLE).orEmpty()
        val repo = TaskRepository(context)
        val tasks = repo.loadTasks().toMutableList()
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index < 0) return
        val task = tasks[index]

        when (intent.action) {
            TaskAlarmScheduler.ACTION_START -> {
                if (task.status == TaskStatus.TODO) {
                    tasks[index] = task.copy(status = TaskStatus.DOING)
                    repo.saveTasks(tasks)
                    TaskNotifier.notifyTaskStarted(context, taskId, title.ifBlank { task.title })
                    context.sendBroadcast(
                        Intent(ACTION_TASKS_CHANGED).setPackage(context.packageName)
                    )
                }
            }
            TaskAlarmScheduler.ACTION_END -> {
                if (task.status != TaskStatus.DONE) {
                    tasks[index] = task.copy(
                        status = if (task.status == TaskStatus.TODO) TaskStatus.DOING else task.status,
                        awaitingCompletion = true,
                        isOverdue = true
                    )
                    repo.saveTasks(tasks)
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
