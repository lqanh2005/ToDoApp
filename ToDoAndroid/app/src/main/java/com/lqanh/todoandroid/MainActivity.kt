package com.lqanh.todoandroid

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.commit
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lqanh.todoandroid.data.NavigationTab
import com.lqanh.todoandroid.data.SettingsRepository
import com.lqanh.todoandroid.data.ThemeMode
import com.lqanh.todoandroid.data.TimeFormat
import com.lqanh.todoandroid.databinding.ActivityMainBinding
import com.lqanh.todoandroid.databinding.DialogSettingsBinding
import com.lqanh.todoandroid.notify.TaskAlarmReceiver
import com.lqanh.todoandroid.notify.TaskNotifier
import com.lqanh.todoandroid.ui.AppUiState
import com.lqanh.todoandroid.ui.TaskViewModel
import com.lqanh.todoandroid.ui.fragments.CreateTaskBottomSheet
import com.lqanh.todoandroid.ui.fragments.ScheduleFragment
import com.lqanh.todoandroid.ui.fragments.TaskDetailFragment
import com.lqanh.todoandroid.ui.fragments.TasksFragment
import kotlinx.coroutines.launch

// Activity duy nhất của app: chứa header, bottom navigation, nút thêm task và vùng hiển thị các Fragment.
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: TaskViewModel by viewModels()

    // Launcher xin quyền gửi thông báo (Android 13+).
    private val requestNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    // Nhận broadcast khi task bị thay đổi từ nền (báo thức/nút trên thông báo) để tải lại dữ liệu.
    private val tasksChangedReceiver = object : BroadcastReceiver() {
        // Đọc lại task từ SQLite để giao diện khớp với thay đổi vừa xảy ra ở nền.
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.reloadFromStorage()
        }
    }

    private var lastScreenKey: String? = null
    private var lastChildScreen: Boolean? = null

    // Khởi tạo giao diện, gắn sự kiện cho các nút, xử lý nút Back và lắng nghe trạng thái từ ViewModel để vẽ lại màn hình.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        TaskNotifier.ensureChannel(this)
        if (SettingsRepository(this).notificationsEnabled) maybeRequestNotificationPermission()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = bars.top, bottom = bars.bottom)
            insets
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val tab = when (item.itemId) {
                R.id.nav_schedule -> NavigationTab.LICH_TRINH
                else -> NavigationTab.CONG_VIEC
            }
            viewModel.selectTab(tab)
            true
        }

        binding.fabAdd.setOnClickListener { showCreateTaskSheet() }
        binding.btnBack.setOnClickListener { viewModel.closeChild() }
        binding.btnMenu.setOnClickListener { showMenu() }
        binding.btnBack.isVisible = false

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            // Đang ở màn chi tiết thì quay về danh sách, ngược lại để hệ thống xử lý (thoát app).
            override fun handleOnBackPressed() {
                if (viewModel.uiState.value.isChildScreen) {
                    viewModel.closeChild()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.completionPrompt.collect { task ->
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle(R.string.completion_title)
                            .setMessage(getString(R.string.completion_message, task.title))
                            .setPositiveButton(R.string.completion_done) { _, _ ->
                                viewModel.resolveCompletion(task.id, true)
                            }
                            .setNegativeButton(R.string.completion_not_done) { _, _ ->
                                viewModel.resolveCompletion(task.id, false)
                            }
                            .setNeutralButton(R.string.completion_later, null)
                            .show()
                    }
                }
                viewModel.uiState.collect { state ->
                    binding.tvTitle.setText(state.headerTitleRes)

                    if (lastChildScreen != state.isChildScreen) {
                        val animate = lastChildScreen != null
                        lastChildScreen = state.isChildScreen
                        setChromeVisible(!state.isChildScreen, animate)
                    }

                    if (!state.isChildScreen) {
                        val navId = when (state.currentTab) {
                            NavigationTab.CONG_VIEC -> R.id.nav_tasks
                            NavigationTab.LICH_TRINH -> R.id.nav_schedule
                        }
                        if (binding.bottomNav.selectedItemId != navId) {
                            binding.bottomNav.selectedItemId = navId
                        }
                    }

                    renderScreen(state)
                }
            }
        }
    }

    // Đăng ký nhận broadcast thay đổi task và tải lại dữ liệu mỗi khi app hiện lên.
    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(TaskAlarmReceiver.ACTION_TASKS_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(tasksChangedReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(tasksChangedReceiver, filter)
        }
        viewModel.reloadFromStorage()
    }

    // Hủy đăng ký broadcast khi app bị ẩn.
    override fun onStop() {
        super.onStop()
        runCatching { unregisterReceiver(tasksChangedReceiver) }
    }

    // Xin quyền thông báo nếu chạy trên Android 13+ và chưa được cấp.
    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Mở bottom sheet tạo task mới (không mở trùng nếu đang hiển thị).
    private fun showCreateTaskSheet() {
        if (supportFragmentManager.findFragmentByTag(CreateTaskBottomSheet.TAG) != null) return
        CreateTaskBottomSheet().show(supportFragmentManager, CreateTaskBottomSheet.TAG)
    }

    // Menu ở góc phải header: Tìm kiếm và Cài đặt.
    private fun showMenu() {
        PopupMenu(this, binding.btnMenu).apply {
            menu.add(0, MENU_SEARCH, 0, R.string.search)
            menu.add(0, MENU_SETTINGS, 1, R.string.settings)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    MENU_SEARCH -> viewModel.toggleSearch()
                    MENU_SETTINGS -> showSettingsDialog()
                }
                true
            }
            show()
        }
    }

    // Hộp thoại Cài đặt: chọn giao diện sáng/tối, định dạng giờ, bật/tắt thông báo và rung.
    // Mỗi lựa chọn được lưu ngay vào SharedPreferences rồi áp dụng (đổi theme/định dạng giờ sẽ vẽ lại màn hình).
    private fun showSettingsDialog() {
        val settings = SettingsRepository(this)
        val dialogBinding = DialogSettingsBinding.inflate(layoutInflater)

        dialogBinding.rgTheme.check(
            when (settings.themeMode) {
                ThemeMode.SYSTEM -> R.id.rbThemeSystem
                ThemeMode.LIGHT -> R.id.rbThemeLight
                ThemeMode.DARK -> R.id.rbThemeDark
            }
        )
        dialogBinding.rgTimeFormat.check(
            when (settings.timeFormat) {
                TimeFormat.H24 -> R.id.rbTime24
                TimeFormat.H12 -> R.id.rbTime12
            }
        )
        dialogBinding.switchNotifications.isChecked = settings.notificationsEnabled
        dialogBinding.switchHaptic.isChecked = viewModel.uiState.value.hapticEnabled

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.settings)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.settings_close, null)
            .create()

        dialogBinding.switchHaptic.setOnCheckedChangeListener { _, checked ->
            viewModel.setHapticEnabled(checked)
        }
        dialogBinding.switchNotifications.setOnCheckedChangeListener { _, checked ->
            settings.notificationsEnabled = checked
            if (checked) {
                maybeRequestNotificationPermission()
            } else {
                NotificationManagerCompat.from(this).cancelAll()
            }
        }
        dialogBinding.rgTimeFormat.setOnCheckedChangeListener { _, checkedId ->
            settings.timeFormat = if (checkedId == R.id.rbTime12) TimeFormat.H12 else TimeFormat.H24
            dialog.dismiss()
            recreate()
        }
        dialogBinding.rgTheme.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.rbThemeLight -> ThemeMode.LIGHT
                R.id.rbThemeDark -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
            settings.themeMode = mode
            dialog.dismiss()
            SettingsRepository.applyTheme(mode)
        }
        dialog.show()
    }

    // Ẩn/hiện header, bottom navigation và nút thêm (có hiệu ứng trượt + mờ dần).
    private fun setChromeVisible(show: Boolean, animate: Boolean) {
        val views = listOf(binding.header, binding.headerDivider, binding.bottomNav, binding.fabAdd)
        if (!animate) {
            views.forEach { view ->
                view.animate().cancel()
                view.alpha = 1f
                view.translationY = 0f
                view.isVisible = show
            }
            return
        }

        val duration = 220L
        views.forEach { view ->
            view.animate().cancel()
            val isBottom = view === binding.bottomNav || view === binding.fabAdd
            if (show) {
                view.isVisible = true
                view.alpha = 0f
                view.translationY = if (isBottom) 28f else -16f
                view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(duration)
                    .setInterpolator(FastOutSlowInInterpolator())
                    .start()
            } else {
                view.animate()
                    .alpha(0f)
                    .translationY(if (isBottom) 28f else -16f)
                    .setDuration(180L)
                    .setInterpolator(FastOutLinearInInterpolator())
                    .withEndAction {
                        view.isVisible = false
                        view.alpha = 1f
                        view.translationY = 0f
                    }
                    .start()
            }
        }
    }

    // Chuyển Fragment theo trạng thái: tab Công việc, tab Lịch trình hoặc màn Chi tiết task, kèm hiệu ứng chuyển cảnh.
    private fun renderScreen(state: AppUiState) {
        val key = state.selectedTaskId?.let { "detail:$it" } ?: "tab:${state.currentTab}"
        if (key == lastScreenKey) return
        val previousKey = lastScreenKey
        lastScreenKey = key

        val goingToDetail = state.selectedTaskId != null
        val comingFromDetail = previousKey?.startsWith("detail:") == true
        val isTabSwitch = !goingToDetail && !comingFromDetail && previousKey != null

        supportFragmentManager.commit {
            setReorderingAllowed(true)

            var tasks = supportFragmentManager.findFragmentByTag(TAG_TASKS)
            var schedule = supportFragmentManager.findFragmentByTag(TAG_SCHEDULE)
            val detail = supportFragmentManager.findFragmentByTag(TAG_DETAIL)

            if (tasks == null) {
                tasks = TasksFragment().also {
                    add(R.id.fragmentContainer, it, TAG_TASKS)
                    if (state.currentTab != NavigationTab.CONG_VIEC || goingToDetail) hide(it)
                }
            }
            if (schedule == null) {
                schedule = ScheduleFragment().also {
                    add(R.id.fragmentContainer, it, TAG_SCHEDULE)
                    if (state.currentTab != NavigationTab.LICH_TRINH || goingToDetail) hide(it)
                }
            }

            val tasksFrag = tasks!!
            val scheduleFrag = schedule!!

            when {
                goingToDetail -> {
                    setCustomAnimations(R.anim.slide_up_in, R.anim.fade_out)
                    if (!tasksFrag.isHidden) hide(tasksFrag)
                    if (!scheduleFrag.isHidden) hide(scheduleFrag)
                    detail?.let { remove(it) }
                    add(
                        R.id.fragmentContainer,
                        TaskDetailFragment.newInstance(state.selectedTaskId!!),
                        TAG_DETAIL
                    )
                }

                comingFromDetail -> {
                    setCustomAnimations(R.anim.fade_in, R.anim.slide_down_out)
                    detail?.let { remove(it) }
                    when (state.currentTab) {
                        NavigationTab.CONG_VIEC -> {
                            if (tasksFrag.isHidden) show(tasksFrag)
                            if (!scheduleFrag.isHidden) hide(scheduleFrag)
                        }
                        NavigationTab.LICH_TRINH -> {
                            if (scheduleFrag.isHidden) show(scheduleFrag)
                            if (!tasksFrag.isHidden) hide(tasksFrag)
                        }
                    }
                }

                isTabSwitch -> {
                    setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    detail?.let { remove(it) }
                    when (state.currentTab) {
                        NavigationTab.CONG_VIEC -> {
                            if (!scheduleFrag.isHidden) hide(scheduleFrag)
                            if (tasksFrag.isHidden) show(tasksFrag)
                        }
                        NavigationTab.LICH_TRINH -> {
                            if (!tasksFrag.isHidden) hide(tasksFrag)
                            if (scheduleFrag.isHidden) show(scheduleFrag)
                        }
                    }
                }

                else -> {
                    detail?.let { remove(it) }
                    when (state.currentTab) {
                        NavigationTab.CONG_VIEC -> {
                            if (tasksFrag.isHidden) show(tasksFrag)
                            if (!scheduleFrag.isHidden) hide(scheduleFrag)
                        }
                        NavigationTab.LICH_TRINH -> {
                            if (scheduleFrag.isHidden) show(scheduleFrag)
                            if (!tasksFrag.isHidden) hide(tasksFrag)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG_TASKS = "tab_tasks"
        private const val TAG_SCHEDULE = "tab_schedule"
        private const val TAG_DETAIL = "task_detail"
        private const val MENU_SEARCH = 1
        private const val MENU_SETTINGS = 2
    }
}
