package com.lqanh.todoandroid

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lqanh.todoandroid.data.NavigationTab
import com.lqanh.todoandroid.databinding.ActivityMainBinding
import com.lqanh.todoandroid.ui.TaskViewModel
import com.lqanh.todoandroid.ui.fragments.CreateTaskFragment
import com.lqanh.todoandroid.ui.fragments.ScheduleFragment
import com.lqanh.todoandroid.ui.fragments.SettingsFragment
import com.lqanh.todoandroid.ui.fragments.StatisticsFragment
import com.lqanh.todoandroid.ui.fragments.TaskDetailFragment
import com.lqanh.todoandroid.ui.fragments.TasksFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = bars.top, bottom = bars.bottom)
            insets
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val tab = when (item.itemId) {
                R.id.nav_tasks -> NavigationTab.CONG_VIEC
                R.id.nav_schedule -> NavigationTab.LICH_TRINH
                R.id.nav_stats -> NavigationTab.THONG_KE
                else -> NavigationTab.CAI_DAT
            }
            viewModel.selectTab(tab)
            true
        }

        binding.fabAdd.setOnClickListener { viewModel.openCreate() }
        binding.btnBack.setOnClickListener { viewModel.closeChild() }
        binding.btnSearch.setOnClickListener { viewModel.toggleSearch() }
        binding.btnProfile.setOnClickListener {
            viewModel.closeChild()
            viewModel.selectTab(NavigationTab.CAI_DAT)
            binding.bottomNav.selectedItemId = R.id.nav_settings
        }

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
                viewModel.uiState.collect { state ->
                    binding.tvTitle.text = state.headerTitle
                    binding.btnBack.isVisible = state.isChildScreen
                    binding.btnSearch.isVisible =
                        !state.isChildScreen && state.currentTab == NavigationTab.CONG_VIEC
                    binding.fabAdd.isVisible = !state.isChildScreen
                    binding.bottomNav.isVisible = !state.isChildScreen

                    if (!state.isChildScreen) {
                        val navId = when (state.currentTab) {
                            NavigationTab.CONG_VIEC -> R.id.nav_tasks
                            NavigationTab.LICH_TRINH -> R.id.nav_schedule
                            NavigationTab.THONG_KE -> R.id.nav_stats
                            NavigationTab.CAI_DAT -> R.id.nav_settings
                        }
                        if (binding.bottomNav.selectedItemId != navId) {
                            binding.bottomNav.selectedItemId = navId
                        }
                    }

                    renderScreen(state.isCreatingTask, state.selectedTaskId, state.currentTab)
                }
            }
        }
    }

    private var lastKey: String? = null

    private fun renderScreen(creating: Boolean, taskId: String?, tab: NavigationTab) {
        val key = when {
            creating -> "create"
            taskId != null -> "detail:$taskId"
            else -> "tab:$tab"
        }
        if (key == lastKey) return
        lastKey = key

        val fragment = when {
            creating -> CreateTaskFragment()
            taskId != null -> TaskDetailFragment()
            tab == NavigationTab.CONG_VIEC -> TasksFragment()
            tab == NavigationTab.LICH_TRINH -> ScheduleFragment()
            tab == NavigationTab.THONG_KE -> StatisticsFragment()
            else -> SettingsFragment()
        }

        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, fragment)
        }
    }
}
