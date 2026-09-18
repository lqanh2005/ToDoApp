package com.lqanh.todoandroid.ui

import android.app.Application
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lqanh.todoandroid.data.NavigationTab
import com.lqanh.todoandroid.data.Task
import com.lqanh.todoandroid.data.TaskRepository
import com.lqanh.todoandroid.data.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUiState(
    val tasks: List<Task> = emptyList(),
    val currentTab: NavigationTab = NavigationTab.CONG_VIEC,
    val searchQuery: String = "",
    val isSearchOpen: Boolean = false,
    val hapticEnabled: Boolean = true
) {
    val isChildScreen: Boolean
        get() = false

    val headerTitle: String
        get() = when (currentTab) {
            NavigationTab.LICH_TRINH -> "Lịch Trình"
            NavigationTab.CONG_VIEC -> "Công Việc"
        }
}

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository(application)

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(tasks = repository.loadTasks()) }
    }

    private fun persist(tasks: List<Task>) {
        viewModelScope.launch {
            repository.saveTasks(tasks)
        }
    }

    private fun haptic() {
        if (!_uiState.value.hapticEnabled) return
        val context = getApplication<Application>()
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
        vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun selectTab(tab: NavigationTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun closeChild() {
        // giữ API cũ cho nút back nếu cần
    }

    fun toggleSearch() {
        _uiState.update {
            val open = !it.isSearchOpen
            it.copy(isSearchOpen = open, searchQuery = if (open) it.searchQuery else "")
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSearchOpen(open: Boolean) {
        _uiState.update {
            it.copy(isSearchOpen = open, searchQuery = if (open) it.searchQuery else "")
        }
    }

    fun toggleTaskComplete(taskId: String) {
        haptic()
        _uiState.update { state ->
            val tasks = state.tasks.map { task ->
                if (task.id != taskId) task
                else {
                    val next = if (task.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE
                    task.copy(
                        status = next,
                        completedAt = if (next == TaskStatus.DONE) "17:00" else null
                    )
                }
            }
            persist(tasks)
            state.copy(tasks = tasks)
        }
    }

    fun changeTaskStatus(taskId: String, status: TaskStatus) {
        haptic()
        _uiState.update { state ->
            val tasks = state.tasks.map {
                if (it.id == taskId) it.copy(status = status) else it
            }
            persist(tasks)
            state.copy(tasks = tasks)
        }
    }

    fun createTask(task: Task) {
        haptic()
        _uiState.update { state ->
            val tasks = listOf(task) + state.tasks
            persist(tasks)
            state.copy(tasks = tasks)
        }
    }

    fun deleteTask(taskId: String) {
        haptic()
        _uiState.update { state ->
            val tasks = state.tasks.filter { it.id != taskId }
            persist(tasks)
            state.copy(tasks = tasks)
        }
    }
}
