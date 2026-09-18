package com.lqanh.todoandroid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.lqanh.todoandroid.R
import com.lqanh.todoandroid.data.PriorityLevel
import com.lqanh.todoandroid.data.Task
import com.lqanh.todoandroid.data.TaskStatus
import com.lqanh.todoandroid.databinding.FragmentTasksBinding
import com.lqanh.todoandroid.ui.TaskViewModel
import com.lqanh.todoandroid.ui.adapters.TaskAdapter
import kotlinx.coroutines.launch

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()

    private var category = "all"
    private var sortMode = 0

    private val adapter by lazy {
        TaskAdapter(
            onToggleComplete = { viewModel.toggleTaskComplete(it) },
            onChangeStatus = { id, status -> viewModel.changeTaskStatus(id, status) },
            onDelete = { viewModel.deleteTask(it) }
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = adapter

        binding.chipGroupCategory.setOnCheckedStateChangeListener { _, checked ->
            category = when (checked.firstOrNull()) {
                R.id.chipWork -> "work"
                R.id.chipPersonal -> "personal"
                R.id.chipStudy -> "study"
                R.id.chipShopping -> "shopping"
                else -> "all"
            }
            refreshList()
        }

        binding.btnSort.setOnClickListener { showSortMenu() }

        binding.btnCloseSearch.setOnClickListener { viewModel.setSearchOpen(false) }
        binding.etSearch.doAfterTextChanged { viewModel.setSearchQuery(it?.toString().orEmpty()) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.searchBar.isVisible = state.isSearchOpen
                    if (state.isSearchOpen && binding.etSearch.text?.toString() != state.searchQuery) {
                        binding.etSearch.setText(state.searchQuery)
                        binding.etSearch.setSelection(state.searchQuery.length)
                    }
                    val done = state.tasks.count { it.status == TaskStatus.DONE }
                    val total = state.tasks.size
                    val percent = if (total == 0) 0 else (done * 100 / total)
                    binding.tvProgressPercent.text = "$percent%"
                    binding.tvProgressDesc.text = getString(R.string.today_progress, done, total)
                    binding.progressToday.progress = percent
                    updateCategoryCounts(state.tasks)
                    refreshList(state.tasks, state.searchQuery)
                }
            }
        }
    }

    private fun showSortMenu() {
        PopupMenu(requireContext(), binding.btnSort).apply {
            menu.add(0, 0, 0, "Ưu tiên")
            menu.add(0, 1, 1, "Deadline")
            menu.add(0, 2, 2, "Mới nhất")
            menu.add(0, 3, 3, "Cũ nhất")
            menu.add(0, 4, 4, "Tùy chỉnh")
            setOnMenuItemClickListener { item ->
                sortMode = item.itemId
                refreshList()
                true
            }
            show()
        }
    }

    private fun updateCategoryCounts(tasks: List<Task>) {
        val all = tasks.size
        val work = tasks.count { it.category == "work" }
        val personal = tasks.count { it.category == "personal" }
        val study = tasks.count { it.category == "study" }
        val shopping = tasks.count { it.category == "shopping" }
        binding.chipAll.text = "Tất cả $all"
        binding.chipWork.text = "Công việc $work"
        binding.chipPersonal.text = "Cá nhân $personal"
        binding.chipStudy.text = "Học tập $study"
        binding.chipShopping.text = "Mua sắm $shopping"
    }

    private fun refreshList(
        tasks: List<Task> = viewModel.uiState.value.tasks,
        query: String = viewModel.uiState.value.searchQuery
    ) {
        val priorityScore = mapOf(PriorityLevel.CAO to 3, PriorityLevel.TB to 2, PriorityLevel.THAP to 1)
        val filtered = tasks
            .filter { category == "all" || it.category == category }
            .filter {
                if (query.isBlank()) true
                else {
                    val q = query.lowercase()
                    it.title.lowercase().contains(q) ||
                        it.description.orEmpty().lowercase().contains(q) ||
                        it.categoryLabel.lowercase().contains(q)
                }
            }
            .sortedWith(
                when (sortMode) {
                    1 -> compareBy { it.dueTime }
                    2 -> compareByDescending { it.id }
                    3 -> compareBy { it.id }
                    4 -> compareBy { it.title }
                    else -> compareByDescending { priorityScore[it.priority] ?: 0 }
                }
            )
        adapter.submitList(filtered)
        binding.tvEmpty.isVisible = filtered.isEmpty()
        binding.rvTasks.isVisible = filtered.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
