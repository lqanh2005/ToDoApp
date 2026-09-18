package com.lqanh.todoandroid.data

import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

object TaskDateUtils {

    const val DEFAULT_DURATION_MS = 60L * 60L * 1000L

    fun startOfDay(millis: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun isSameDay(a: Long, b: Long): Boolean {
        val ca = Calendar.getInstance().apply { timeInMillis = a }
        val cb = Calendar.getInstance().apply { timeInMillis = b }
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
            ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
    }

    fun resolveDueDayMillis(task: Task): Long? {
        task.dueAtMillis?.let { return startOfDay(it) }
        return parseLabelToDay(task.dueDate.ifBlank { task.dueTime })
    }

    fun parseLabelToDay(raw: String): Long? {
        val text = raw.trim().lowercase(Locale.getDefault())
        if (text.isBlank()) return null
        val today = startOfDay()
        when {
            text.contains("hôm nay") || text.contains("hom nay") -> return today
            text.contains("ngày mai") || text.contains("ngay mai") -> {
                return Calendar.getInstance().apply {
                    timeInMillis = today
                    add(Calendar.DAY_OF_YEAR, 1)
                }.timeInMillis
            }
        }
        val matcher = Pattern.compile("(\\d{1,2})\\s*th\\s*(\\d{1,2})", Pattern.CASE_INSENSITIVE)
            .matcher(raw)
        if (matcher.find()) {
            val day = matcher.group(1)?.toIntOrNull() ?: return null
            val month = matcher.group(2)?.toIntOrNull() ?: return null
            val year = Calendar.getInstance().get(Calendar.YEAR)
            return Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        return null
    }

    fun parseTimeOfDay(raw: String): Pair<Int, Int>? {
        val matcher = Pattern.compile("(\\d{1,2}):(\\d{2})").matcher(raw)
        if (!matcher.find()) return null
        val hour = matcher.group(1)?.toIntOrNull() ?: return null
        val minute = matcher.group(2)?.toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return hour to minute
    }

    fun resolveStartAtMillis(task: Task): Long? {
        task.startAtMillis?.let { return it }
        val day = resolveDueDayMillis(task) ?: return null
        val time = parseTimeOfDay(task.dueTime.ifBlank { task.dueDate }) ?: return null
        return Calendar.getInstance().apply {
            timeInMillis = day
            set(Calendar.HOUR_OF_DAY, time.first)
            set(Calendar.MINUTE, time.second)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun resolveEndAtMillis(task: Task): Long? {
        task.endAtMillis?.let { return it }
        val start = resolveStartAtMillis(task) ?: return null
        return start + DEFAULT_DURATION_MS
    }

    fun tasksOnDay(tasks: List<Task>, dayMillis: Long): List<Task> {
        val day = startOfDay(dayMillis)
        return tasks.filter { task ->
            val due = resolveDueDayMillis(task) ?: return@filter false
            isSameDay(due, day)
        }
    }

    fun daysWithTasks(tasks: List<Task>): Set<Long> {
        return tasks.mapNotNull { resolveDueDayMillis(it) }.toSet()
    }

    fun buildStartEnd(
        dueDayMillis: Long?,
        hasTime: Boolean,
        hour: Int,
        minute: Int
    ): Pair<Long?, Long?> {
        if (dueDayMillis == null || !hasTime) return null to null
        val start = Calendar.getInstance().apply {
            timeInMillis = startOfDay(dueDayMillis)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return start to (start + DEFAULT_DURATION_MS)
    }
}
