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
import com.lqanh.todoandroid.databinding.ActivityMainBinding
import com.lqanh.todoandroid.notify.TaskAlarmReceiver
import com.lqanh.todoandroid.notify.TaskNotifier
import com.lqanh.todoandroid.ui.AppUiState
import com.lqanh.todoandroid.ui.TaskViewModel
import com.lqanh.todoandroid.ui.fragments.CreateTaskBottomSheet
import com.lqanh.todoandroid.ui.fragments.ScheduleFragment
import com.lqanh.todoandroid.ui.fragments.TaskDetailFragment
import com.lqanh.todoandroid.ui.fragments.TasksFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: TaskViewModel by viewModels()

    private val requestNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private val tasksChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.reloadFromStorage()
        }
    }

    private var lastScreenKey: String? = null
    private var lastChildScreen: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        TaskNotifier.ensureChannel(this)
        maybeRequestNotificationPermission()

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
        binding.btnMenu.isVisible = false
        binding.btnBack.isVisible = false

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
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
                            .setTitle("Task time is up")
                            .setMessage("\"${task.title}\" has ended.\nHave you completed it?")
                            .setPositiveButton("Done") { _, _ ->
                                viewModel.resolveCompletion(task.id, true)
                            }
                            .setNegativeButton("Not done") { _, _ ->
                                viewModel.resolveCompletion(task.id, false)
                            }
                            .setNeutralButton("Later", null)
                            .show()
                    }
                }
                viewModel.uiState.collect { state ->
                    binding.tvTitle.text = state.headerTitle

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

    override fun onStop() {
        super.onStop()
        runCatching { unregisterReceiver(tasksChangedReceiver) }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun showCreateTaskSheet() {
        if (supportFragmentManager.findFragmentByTag(CreateTaskBottomSheet.TAG) != null) return
        CreateTaskBottomSheet().show(supportFragmentManager, CreateTaskBottomSheet.TAG)
    }

    private fun showMenu() {
        PopupMenu(this, binding.btnMenu).apply {
            menu.add(0, 1, 0, "Search")
            setOnMenuItemClickListener { item ->
                if (item.itemId == 1) viewModel.toggleSearch()
                true
            }
            show()
        }
    }

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
    }
}
