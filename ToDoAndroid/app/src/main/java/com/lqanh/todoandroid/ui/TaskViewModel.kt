package com.lqanh.todoandroid.ui

import android.app.Application
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lqanh.todoandroid.R
import com.lqanh.todoandroid.data.NavigationTab
import com.lqanh.todoandroid.data.SettingsRepository
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

// Trạng thái giao diện toàn app: danh sách task, tab hiện tại, tìm kiếm, cài đặt rung và task đang mở.
data class AppUiState(
    val tasks: List<Task> = emptyList(),
    val currentTab: NavigationTab = NavigationTab.CONG_VIEC,
    val searchQuery: String = "",
    val isSearchOpen: Boolean = false,
    val hapticEnabled: Boolean = true,
    val selectedTaskId: String? = null
) {
    // true khi đang mở màn chi tiết task.
    val isChildScreen: Boolean
        get() = selectedTaskId != null

    // Chuỗi tiêu đề hiển thị trên header tùy theo màn hình hiện tại.
    @get:StringRes
    val headerTitleRes: Int
        get() = when {
            selectedTaskId != null -> R.string.header_task
            currentTab == NavigationTab.LICH_TRINH -> R.string.tab_schedule
            else -> R.string.tab_tasks
        }
}

// ViewModel dùng chung cho mọi màn hình: giữ danh sách task, xử lý thao tác người dùng và lưu xuống SQLite.
class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository(application)
    private val settings = SettingsRepository(application)

    private val _uiState = MutableStateFlow(AppUiState(hapticEnabled = settings.hapticEnabled))
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    // Luồng sự kiện yêu cầu hiện hộp thoại hỏi "đã hoàn thành chưa" khi task hết giờ.
    private val _completionPrompt = MutableSharedFlow<Task>(extraBufferCapacity = 1)
    val completionPrompt: SharedFlow<Task> = _completionPrompt.asSharedFlow()

    // Khi ViewModel được tạo: đọc task từ SQLite, cập nhật trạng thái theo giờ hiện tại, lưu lại và đặt lịch báo thức.
    init {
        val tasks = syncTimedStatuses(repository.loadTasks())
        repository.saveTasks(tasks)
        runCatching { TaskAlarmScheduler.scheduleAll(application, tasks) }
        _uiState.update { it.copy(tasks = tasks) }
    }

    // Đọc lại dữ liệu từ SQLite (ví dụ sau khi BroadcastReceiver sửa task lúc app chạy nền)
    // và hiện hộp thoại hỏi hoàn thành cho các task đã hết giờ.
    fun reloadFromStorage() {
        val tasks = syncTimedStatuses(repository.loadTasks())
        repository.saveTasks(tasks)
        runCatching { TaskAlarmScheduler.scheduleAll(getApplication(), tasks) }
        _uiState.update { it.copy(tasks = tasks) }
        tasks.filter { it.awaitingCompletion }.forEach { _completionPrompt.tryEmit(it) }
    }

    // Ghi danh sách task xuống SQLite và đặt lại lịch báo thức cho các task.
    private fun persist(tasks: List<Task>) {
        viewModelScope.launch {
            repository.saveTasks(tasks)
            runCatching { TaskAlarmScheduler.scheduleAll(getApplication(), tasks) }
        }
    }

    // Tự chuyển trạng thái theo thời gian: đến giờ bắt đầu -> DOING, quá giờ kết thúc -> đánh dấu quá hạn và chờ xác nhận.
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

    // Rung nhẹ 30ms khi thao tác, chỉ chạy nếu người dùng bật rung trong Cài đặt.
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

    // Bật/tắt rung và lưu lựa chọn vào SharedPreferences.
    fun setHapticEnabled(enabled: Boolean) {
        settings.hapticEnabled = enabled
        _uiState.update { it.copy(hapticEnabled = enabled) }
    }

    // Chuyển tab ở bottom navigation và đóng màn chi tiết nếu đang mở.
    fun selectTab(tab: NavigationTab) {
        _uiState.update { it.copy(currentTab = tab, selectedTaskId = null) }
    }

    // Mở màn chi tiết của một task.
    fun openTaskDetail(taskId: String) {
        _uiState.update { it.copy(selectedTaskId = taskId) }
    }

    // Đóng màn chi tiết, quay về tab đang chọn.
    fun closeChild() {
        _uiState.update { it.copy(selectedTaskId = null) }
    }

    // Thay thế một task bằng phiên bản đã chỉnh sửa rồi lưu lại.
    fun updateTask(task: Task) {
        _uiState.update { state ->
            val tasks = state.tasks.map { if (it.id == task.id) task else it }
                .let { syncTimedStatuses(it) }
            persist(tasks)
            state.copy(tasks = tasks)
        }
    }

    // Bật/tắt thanh tìm kiếm; khi tắt thì xóa từ khóa.
    fun toggleSearch() {
        _uiState.update {
            val open = !it.isSearchOpen
            it.copy(isSearchOpen = open, searchQuery = if (open) it.searchQuery else "")
        }
    }

    // Cập nhật từ khóa tìm kiếm.
    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    // Mở hoặc đóng thanh tìm kiếm; khi đóng thì xóa từ khóa.
    fun setSearchOpen(open: Boolean) {
        _uiState.update {
            it.copy(isSearchOpen = open, searchQuery = if (open) it.searchQuery else "")
        }
    }

    // Đảo trạng thái hoàn thành của task (DONE <-> TODO) khi bấm checkbox.
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

    // Đặt trạng thái cụ thể cho task (TODO/DOING/DONE).
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

    // Bật/tắt đánh dấu sao (task quan trọng).
    fun toggleStar(taskId: String) {
        haptic()
        _uiState.update { state ->
            val tasks = state.tasks.map {
                if (it.id == taskId) it.copy(isStarred = !it.isStarred) else it
            }
            persist(tasks)
            state.copy(tasks = tasks)
        }
    }

    // Xử lý câu trả lời của người dùng khi task hết giờ: đã xong -> DONE, chưa xong -> TODO và giữ cờ quá hạn.
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

    // Cập nhật ngày/giờ/nhắc nhở của task, reset cờ quá hạn và tính lại trạng thái theo thời gian.
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

    // Thêm task mới vào đầu danh sách rồi lưu lại.
    fun createTask(task: Task) {
        haptic()
        _uiState.update { state ->
            val synced = syncTimedStatuses(listOf(task) + state.tasks)
            persist(synced)
            state.copy(tasks = synced)
        }
    }

    // Xóa task, hủy báo thức liên quan rồi lưu lại.
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
