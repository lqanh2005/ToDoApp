package com.lqanh.todoandroid.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TaskRepository(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadTasks(): List<Task> {
        val json = prefs.getString(KEY_TASKS, null) ?: return InitialTasks.list
        return try {
            val type = object : TypeToken<List<Task>>() {}.type
            gson.fromJson<List<Task>>(json, type) ?: InitialTasks.list
        } catch (_: Exception) {
            InitialTasks.list
        }
    }

    fun saveTasks(tasks: List<Task>) {
        prefs.edit().putString(KEY_TASKS, gson.toJson(tasks)).apply()
    }

    fun resetTasks(): List<Task> {
        saveTasks(InitialTasks.list)
        return InitialTasks.list
    }

    companion object {
        private const val PREFS_NAME = "nhipsang_plan"
        private const val KEY_TASKS = "nhipsang_plan_tasks_v1"
    }
}
