package com.lqanh.todoandroid

import android.os.Bundle
import android.widget.PopupMenu
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
import com.lqanh.todoandroid.ui.fragments.CreateTaskBottomSheet
import com.lqanh.todoandroid.ui.fragments.ScheduleFragment
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
                R.id.nav_schedule -> NavigationTab.LICH_TRINH
                else -> NavigationTab.CONG_VIEC
            }
            viewModel.selectTab(tab)
            true
        }

        binding.fabAdd.setOnClickListener { showCreateTaskSheet() }
        binding.btnBack.setOnClickListener { viewModel.closeChild() }
        binding.btnMenu.setOnClickListener { showMenu() }

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
                    binding.ivLogo.isVisible = !state.isChildScreen
                    binding.tvSubtitle.isVisible = !state.isChildScreen
                    binding.btnMenu.isVisible =
                        !state.isChildScreen && state.currentTab == NavigationTab.CONG_VIEC
                    binding.fabAdd.isVisible = !state.isChildScreen
                    binding.bottomNav.isVisible = !state.isChildScreen

                    if (!state.isChildScreen) {
                        val navId = when (state.currentTab) {
                            NavigationTab.CONG_VIEC -> R.id.nav_tasks
                            NavigationTab.LICH_TRINH -> R.id.nav_schedule
                        }
                        if (binding.bottomNav.selectedItemId != navId) {
                            binding.bottomNav.selectedItemId = navId
                        }
                    }

                    renderScreen(state.currentTab)
                }
            }
        }
    }

    private fun showCreateTaskSheet() {
        if (supportFragmentManager.findFragmentByTag(CreateTaskBottomSheet.TAG) != null) return
        CreateTaskBottomSheet().show(supportFragmentManager, CreateTaskBottomSheet.TAG)
    }

    private fun showMenu() {
        PopupMenu(this, binding.btnMenu).apply {
            menu.add(0, 1, 0, "Tìm kiếm")
            setOnMenuItemClickListener { item ->
                if (item.itemId == 1) viewModel.toggleSearch()
                true
            }
            show()
        }
    }

    private var lastTab: NavigationTab? = null

    private fun renderScreen(tab: NavigationTab) {
        if (tab == lastTab) return
        lastTab = tab

        val fragment = when (tab) {
            NavigationTab.LICH_TRINH -> ScheduleFragment()
            NavigationTab.CONG_VIEC -> TasksFragment()
        }

        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, fragment)
        }
    }
}
