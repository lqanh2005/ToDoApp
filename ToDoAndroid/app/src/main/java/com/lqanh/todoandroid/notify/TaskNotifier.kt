package com.lqanh.todoandroid.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lqanh.todoandroid.MainActivity
import com.lqanh.todoandroid.R

// Tạo và quản lý thông báo trạng thái task.
object TaskNotifier {

    const val CHANNEL_ID = "task_status_channel"
    const val ACTION_MARK_DONE = "com.lqanh.todoandroid.ACTION_MARK_DONE"
    const val ACTION_MARK_NOT_DONE = "com.lqanh.todoandroid.ACTION_MARK_NOT_DONE"
    const val EXTRA_TASK_ID = "task_id"

    // Tạo kênh thông báo (bắt buộc từ Android 8 trở lên).
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Task status",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when a task starts or ends"
        }
        manager.createNotificationChannel(channel)
    }

    // Hiện thông báo khi task đến giờ bắt đầu; bấm vào sẽ mở app.
    fun notifyTaskStarted(context: Context, taskId: String, title: String) {
        ensureChannel(context)
        val open = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_check_circle)
            .setContentTitle("In progress")
            .setContentText("It's time: $title")
            .setContentIntent(open)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(notifId(taskId, 1), notification)
    }

    // Hiện thông báo khi task hết giờ, kèm hai nút "Done" và "Not done".
    fun notifyTaskEnded(context: Context, taskId: String, title: String) {
        ensureChannel(context)
        val open = PendingIntent.getActivity(
            context,
            taskId.hashCode() + 11,
            Intent(context, MainActivity::class.java).putExtra(EXTRA_TASK_ID, taskId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val donePi = PendingIntent.getBroadcast(
            context,
            taskId.hashCode() + 21,
            Intent(context, TaskActionReceiver::class.java).apply {
                action = ACTION_MARK_DONE
                putExtra(EXTRA_TASK_ID, taskId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notDonePi = PendingIntent.getBroadcast(
            context,
            taskId.hashCode() + 22,
            Intent(context, TaskActionReceiver::class.java).apply {
                action = ACTION_MARK_NOT_DONE
                putExtra(EXTRA_TASK_ID, taskId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle("Task time is up")
            .setContentText("$title — are you done?")
            .setContentIntent(open)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(0, "Done", donePi)
            .addAction(0, "Not done", notDonePi)
            .build()
        NotificationManagerCompat.from(context).notify(notifId(taskId, 2), notification)
    }

    // Xóa các thông báo đang hiển thị của một task.
    fun cancel(context: Context, taskId: String) {
        val nm = NotificationManagerCompat.from(context)
        nm.cancel(notifId(taskId, 1))
        nm.cancel(notifId(taskId, 2))
    }

    // Sinh id thông báo riêng cho mỗi task và loại thông báo.
    private fun notifId(taskId: String, type: Int): Int {
        return (taskId.hashCode() and 0x7FFFFFFF) * 10 + type
    }
}
