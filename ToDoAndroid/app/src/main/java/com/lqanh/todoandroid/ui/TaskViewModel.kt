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
import com.lqanh.todoandroid.data.TaskDateUtils
import com.lqanh.todoandroid.data.TaskRepository
import com.lqanh.todoandroid.data.TaskStatus
import com.lqanh.todoandroid.notify.TaskAlarmScheduler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _completionPrompt = MutableSharedFlow<Task>(extraBufferCapacity = 1)
    val completionPrompt: SharedFlow<Task> = _completionPrompt.asSharedFlow()

    init {
        val tasks = syncTimedStatuses(repository.loadTasks())
        repository.saveTasks(tasks)
        TaskAlarmScheduler.scheduleAll(application, tasks)
        _uiState.update { it.copy(tasks = tasks) }
    }

    fun reloadFromStorage() {
        val tasks = syncTimedStatuses(repository.loadTasks())
        repository.saveTasks(tasks)
        TaskAlarmScheduler.scheduleAll(getApplication(), tasks)
        _uiState.update { it.copy(tasks = tasks) }
        tasks.filter { it.awaitingCompletion }.forEach { _completionPrompt.tryEmit(it) }
    }

    private fun persist(tasks: List<Task>) {
        viewModelScope.launch {
            repository.saveTasks(tasks)
            TaskAlarmScheduler.scheduleAll(getApplication(), tasks)
        }
    }

    private fun syncTimedStatuses(tasks: List<Task>): List<Task> {
        val now = System.currentTimeMillis()
        return tasks.map { task ->
            if (task.status == TaskStatus.DONE) return@map task.copy(awaitingCompletion = false)
            val startAt = TaskDateUtils.resolveStartAtMillis(task)
            val endAt = TaskDateUtils.resolveEndAtMillis(task)
            var status = task.status
            var awaiting = task.awaitingCompletion
            var overdue = task.isOverdue

            if (startAt != null && now >= startAt && status == TaskStatus.TODO) {
                status = TaskStatus.DOING
            }
            if (endAt != null && now >= endAt && status != TaskStatus.DONE) {
                awaiting = true
                overdue = true
                if (status == TaskStatus.TODO) status = TaskStatus.DOING
            }
            task.copy(status = status, awaitingCompletion = awaiting, isOverdue = overdue)
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
                        awaitingCompletion = false,
                        isOverdue = if (next == TaskStatus.DONE) false else task.isOverdue,
                        completedAt = if (next == TaskStatus.DONE) "now" else null
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
                if (it.id != taskId) it
                else it.copy(
                    status = status,
                    awaitingCompletion = false,
                    isOverdue = if (status == TaskStatus.DONE) false else it.isOverdue,
                    completedAt = if (status == TaskStatus.DONE) "now" else it.completedAt
                )
            }
            persist(tasks)
            state.copy(tasks = tasks)
        }
    }

    fun resolveCompletion(taskId: String, done: Boolean) {
        haptic()
        _uiState.update { state ->
            val tasks = state.tasks.map {
                if (it.id != taskId) it
                else it.copy(
                    status = if (done) TaskStatus.DONE else TaskStatus.TODO,
                    awaitingCompletion = false,
                    isOverdue = !done,
                    completedAt = if (done) "now" else null
                )
            }
            persist(tasks)
            state.copy(tasks = tasks)
        }
    }

    fun updateTaskSchedule(
        taskId: String,
        dueLabel: String,
        dueAtMillis: Long?,
        startAtMillis: Long?,
        endAtMillis: Long?,
        reminder: String
    ) {
        haptic()
        _uiState.update { state ->
            val tasks = state.tasks.map {
                if (it.id != taskId) it
                else it.copy(
                    dueDate = dueLabel,
                    dueTime = dueLabel,
                    dueAtMillis = dueAtMillis,
                    startAtMillis = startAtMillis,
                    endAtMillis = endAtMillis,
                    reminder = reminder,
                    awaitingCompletion = false,
                    isOverdue = false,
                    status = if (it.status == TaskStatus.DONE) it.status else TaskStatus.TODO
                )
            }.let { syncTimedStatuses(it) }
            persist(tasks)
            state.copy(tasks = tasks)
        }
    }

    fun createTask(task: Task) {
        haptic()
        _uiState.update { state ->
            val synced = syncTimedStatuses(listOf(task) + state.tasks)
            persist(synced)
            state.copy(tasks = synced)
        }
    }

    fun deleteTask(taskId: String) {
        haptic()
        TaskAlarmScheduler.cancelForTask(getApplication(), taskId)
        _uiState.update { state ->
            val tasks = state.tasks.filter { it.id != taskId }
            persist(tasks)
            state.copy(tasks = tasks)
        }
    }
}
