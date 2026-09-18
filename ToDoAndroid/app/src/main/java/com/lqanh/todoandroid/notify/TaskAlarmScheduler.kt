package com.lqanh.todoandroid.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.lqanh.todoandroid.data.Task
import com.lqanh.todoandroid.data.TaskDateUtils
import com.lqanh.todoandroid.data.TaskStatus

object TaskAlarmScheduler {

    const val ACTION_START = "com.lqanh.todoandroid.ACTION_TASK_START"
    const val ACTION_END = "com.lqanh.todoandroid.ACTION_TASK_END"
    const val EXTRA_TASK_ID = "task_id"
    const val EXTRA_TASK_TITLE = "task_title"

    fun scheduleForTask(context: Context, task: Task) {
        cancelForTask(context, task.id)
        if (task.status == TaskStatus.DONE) return

        val startAt = TaskDateUtils.resolveStartAtMillis(task)
        val endAt = TaskDateUtils.resolveEndAtMillis(task)
        val now = System.currentTimeMillis()

        if (startAt != null && startAt > now && task.status == TaskStatus.TODO) {
            setExact(context, task, ACTION_START, startAt, requestCode(task.id, 1))
        }
        if (endAt != null && endAt > now && !task.awaitingCompletion) {
            setExact(context, task, ACTION_END, endAt, requestCode(task.id, 2))
        }
    }

    fun scheduleAll(context: Context, tasks: List<Task>) {
        tasks.forEach { scheduleForTask(context, it) }
    }

    fun cancelForTask(context: Context, taskId: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context, ACTION_START, taskId, "", requestCode(taskId, 1)))
        am.cancel(pendingIntent(context, ACTION_END, taskId, "", requestCode(taskId, 2)))
    }

    private fun setExact(context: Context, task: Task, action: String, triggerAt: Long, code: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(context, action, task.id, task.title, code)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    private fun pendingIntent(
        context: Context,
        action: String,
        taskId: String,
        title: String,
        code: Int
    ): PendingIntent {
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, title)
        }
        return PendingIntent.getBroadcast(
            context,
            code,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun requestCode(taskId: String, type: Int): Int {
        return (taskId.hashCode() and 0x7FFFFFFF) * 10 + type
    }
}
