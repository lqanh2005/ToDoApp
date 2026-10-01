package com.lqanh.todoandroid.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.lqanh.todoandroid.data.Task
import com.lqanh.todoandroid.data.TaskDateUtils
import com.lqanh.todoandroid.data.TaskStatus

// Đặt/hủy báo thức hệ thống (AlarmManager) cho thời điểm bắt đầu và kết thúc của task.
object TaskAlarmScheduler {

    const val ACTION_START = "com.lqanh.todoandroid.ACTION_TASK_START"
    const val ACTION_END = "com.lqanh.todoandroid.ACTION_TASK_END"
    const val EXTRA_TASK_ID = "task_id"
    const val EXTRA_TASK_TITLE = "task_title"

    // Đặt lại báo thức cho một task: báo lúc bắt đầu (nếu chưa làm) và lúc kết thúc (nếu chưa xong).
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

    // Đặt báo thức cho toàn bộ danh sách task.
    fun scheduleAll(context: Context, tasks: List<Task>) {
        tasks.forEach { scheduleForTask(context, it) }
    }

    // Hủy cả hai báo thức (bắt đầu/kết thúc) của một task.
    fun cancelForTask(context: Context, taskId: String) {
        try {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.cancel(pendingIntent(context, ACTION_START, taskId, "", requestCode(taskId, 1)))
            am.cancel(pendingIntent(context, ACTION_END, taskId, "", requestCode(taskId, 2)))
        } catch (_: Exception) {
        }
    }

    // Đặt báo thức chính xác; nếu máy không cho phép báo thức chính xác thì dùng báo thức gần đúng.
    private fun setExact(context: Context, task: Task, action: String, triggerAt: Long, code: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(context, action, task.id, task.title, code)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } catch (_: SecurityException) {
            try {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } catch (_: Exception) {
                am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } catch (_: Exception) {
        }
    }

    // Tạo PendingIntent gửi tới TaskAlarmReceiver kèm id và tiêu đề task.
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

    // Sinh mã request riêng cho mỗi task và loại báo thức (1 = bắt đầu, 2 = kết thúc).
    private fun requestCode(taskId: String, type: Int): Int {
        return 31 * (taskId.hashCode() and 0x0FFFFFFF) + type
    }
}
