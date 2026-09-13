package com.lqanh.todoandroid.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lqanh.todoandroid.data.NavigationTab
import com.lqanh.todoandroid.ui.components.AppFab
import com.lqanh.todoandroid.ui.components.AppHeader
import com.lqanh.todoandroid.ui.components.BottomNavBar
import com.lqanh.todoandroid.ui.screens.CreateTaskScreen
import com.lqanh.todoandroid.ui.screens.ScheduleScreen
import com.lqanh.todoandroid.ui.screens.SettingsScreen
import com.lqanh.todoandroid.ui.screens.StatisticsScreen
import com.lqanh.todoandroid.ui.screens.TaskDetailScreen
import com.lqanh.todoandroid.ui.screens.TasksScreen
import com.lqanh.todoandroid.ui.theme.NhipSongTheme
import com.lqanh.todoandroid.ui.theme.Surface

@Composable
fun NhipSongApp(
    viewModel: TaskViewModel = viewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isChildScreen) {
        BackHandler { viewModel.closeChild() }
    }

    NhipSongTheme {
        Scaffold(
            topBar = {
                AppHeader(
                    title = state.headerTitle,
                    showBack = state.isChildScreen,
                    showSearch = !state.isChildScreen && state.currentTab == NavigationTab.CONG_VIEC,
                    onBack = { viewModel.closeChild() },
                    onSearchClick = { viewModel.toggleSearch() },
                    onProfileClick = {
                        viewModel.closeChild()
                        viewModel.selectTab(NavigationTab.CAI_DAT)
                    }
                )
            },
            bottomBar = {
                if (!state.isChildScreen) {
                    BottomNavBar(
                        currentTab = state.currentTab,
                        onSelectTab = { viewModel.selectTab(it) }
                    )
                }
            },
            floatingActionButton = {
                if (!state.isChildScreen) {
                    AppFab(onClick = { viewModel.openCreate() })
                }
            },
            containerColor = Surface
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Surface)
            ) {
                when {
                    state.isCreatingTask -> {
                        CreateTaskScreen(
                            onClose = { viewModel.closeChild() },
                            onCreate = { viewModel.createTask(it) }
                        )
                    }

                    state.selectedTask != null -> {
                        TaskDetailScreen(
                            task = state.selectedTask!!,
                            onUpdate = { viewModel.updateTask(it) },
                            onDelete = { viewModel.deleteTask(it) },
                            onBack = { viewModel.closeChild() }
                        )
                    }

                    else -> {
                        when (state.currentTab) {
                            NavigationTab.CONG_VIEC -> TasksScreen(
                                tasks = state.tasks,
                                searchQuery = state.searchQuery,
                                isSearchOpen = state.isSearchOpen,
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                onCloseSearch = { viewModel.setSearchOpen(false) },
                                onSelectTask = { viewModel.selectTask(it.id) },
                                onToggleComplete = { viewModel.toggleTaskComplete(it) },
                                onChangeStatus = { id, status -> viewModel.changeTaskStatus(id, status) },
                                onDeleteTask = { viewModel.deleteTask(it) },
                                onOpenCreate = { viewModel.openCreate() }
                            )

                            NavigationTab.LICH_TRINH -> ScheduleScreen(
                                tasks = state.tasks,
                                onSelectTask = { viewModel.selectTask(it.id) },
                                onChangeStatus = { id, status -> viewModel.changeTaskStatus(id, status) }
                            )

                            NavigationTab.THONG_KE -> StatisticsScreen(tasks = state.tasks)

                            NavigationTab.CAI_DAT -> SettingsScreen(
                                hapticEnabled = state.hapticEnabled,
                                onHapticChange = { viewModel.setHapticEnabled(it) },
                                onResetData = { viewModel.resetData() }
                            )
                        }
                    }
                }
            }
        }
    }
}
