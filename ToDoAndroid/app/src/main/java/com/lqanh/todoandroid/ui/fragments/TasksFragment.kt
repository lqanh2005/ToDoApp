package com.lqanh.todoandroid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
import com.lqanh.todoandroid.data.TaskDateUtils
import com.lqanh.todoandroid.data.TaskStatus
import com.lqanh.todoandroid.databinding.FragmentTasksBinding
import com.lqanh.todoandroid.ui.TaskViewModel
import com.lqanh.todoandroid.ui.adapters.GroupedTaskAdapter
import com.lqanh.todoandroid.ui.adapters.TaskSection
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()

    private var category = "all"
    private var sortMode = 0
    private val expandedSections = mutableMapOf(
        TaskSection.PREVIOUS to true,
        TaskSection.TODAY to true,
        TaskSection.FUTURE to true,
        TaskSection.COMPLETE to true
    )

    private val adapter by lazy {
        GroupedTaskAdapter(
            onToggleComplete = { viewModel.toggleTaskComplete(it) },
            onChangeStatus = { id, status ->
                if (status == TaskStatus.TODO) viewModel.resolveCompletion(id, false)
                else viewModel.changeTaskStatus(id, status)
            },
            onDelete = { viewModel.deleteTask(it) },
            onEditSchedule = { showScheduleEditor(it) },
            onOpenDetail = { viewModel.openTaskDetail(it.id) },
            onToggleSection = { section ->
                expandedSections[section] = !(expandedSections[section] ?: true)
                refreshList()
            }
        )
    }

    private var editingScheduleTaskId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = adapter

        childFragmentManager.setFragmentResultListener(
            DateScheduleDialogFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val taskId = editingScheduleTaskId ?: return@setFragmentResultListener
            editingScheduleTaskId = null
            val noDate = bundle.getBoolean(DateScheduleDialogFragment.KEY_NO_DATE)
            val dueMillis = if (noDate) null else bundle.getLong(DateScheduleDialogFragment.KEY_MILLIS)
            val hasTime = bundle.getBoolean(DateScheduleDialogFragment.KEY_HAS_TIME)
            val hour = bundle.getInt(DateScheduleDialogFragment.KEY_HOUR)
            val minute = bundle.getInt(DateScheduleDialogFragment.KEY_MINUTE)
            val rem = bundle.getString(DateScheduleDialogFragment.KEY_REMINDER).orEmpty()
            val reminderValue = when {
                rem.isBlank() -> "0"
                rem.contains("At task", true) -> "0"
                rem.startsWith("5") -> "5"
                rem.startsWith("15") -> "15"
                rem.startsWith("30") -> "30"
                rem.startsWith("1 day") -> "1440"
                else -> rem
            }
            val dueLabel = formatDueLabel(dueMillis, hasTime, hour, minute)
            val (start, end) = TaskDateUtils.buildStartEnd(dueMillis, hasTime, hour, minute)
            viewModel.updateTaskSchedule(
                taskId = taskId,
                dueLabel = dueLabel,
                dueAtMillis = dueMillis?.let { TaskDateUtils.startOfDay(it) },
                startAtMillis = start,
                endAtMillis = end,
                reminder = reminderValue
            )
        }

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

        binding.btnCloseSearch.setOnClickListener { viewModel.setSearchOpen(false) }
        binding.etSearch.doAfterTextChanged { viewModel.setSearchQuery(it?.toString().orEmpty()) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.completionPrompt.collect { task ->
                        if (isAdded) showCompletionDialog(task)
                    }
                }
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

    private fun showCompletionDialog(task: Task) {
        AlertDialog.Builder(requireContext())
            .setTitle("Task time is up")
            .setMessage("\"${task.title}\" has ended.\nHave you completed it?")
            .setPositiveButton("Done") { _, _ -> viewModel.resolveCompletion(task.id, true) }
            .setNegativeButton("Not done") { _, _ -> viewModel.resolveCompletion(task.id, false) }
            .setNeutralButton("Later", null)
            .show()
    }

    private fun showScheduleEditor(task: Task) {
        editingScheduleTaskId = task.id
        if (childFragmentManager.findFragmentByTag(DateScheduleDialogFragment.TAG) != null) return
        val initial = task.dueAtMillis
            ?: TaskDateUtils.resolveDueDayMillis(task)
            ?: System.currentTimeMillis()
        DateScheduleDialogFragment
            .newInstance(initial)
            .show(childFragmentManager, DateScheduleDialogFragment.TAG)
    }

    private fun formatDueLabel(
        millis: Long?,
        withTime: Boolean,
        hour: Int,
        minute: Int
    ): String {
        if (millis == null) return ""
        val selected = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val datePart = when (selected.timeInMillis) {
            today.timeInMillis -> "today"
            tomorrow.timeInMillis -> "Tomorrow"
            else -> SimpleDateFormat("MMM d", Locale.ENGLISH).format(Date(millis))
        }
        return if (withTime) "%02d:%02d · %s".format(hour, minute, datePart) else datePart
    }

    private fun updateCategoryCounts(tasks: List<Task>) {
        val all = tasks.size
        val work = tasks.count { it.category == "work" }
        val personal = tasks.count { it.category == "personal" }
        val study = tasks.count { it.category == "study" }
        val shopping = tasks.count { it.category == "shopping" }
        binding.chipAll.text = "All $all"
        binding.chipWork.text = "Work $work"
        binding.chipPersonal.text = "Personal $personal"
        binding.chipStudy.text = "Study $study"
        binding.chipShopping.text = "Shopping $shopping"
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

        val items = GroupedTaskAdapter.buildItems(filtered, expandedSections)
        adapter.submitList(items)
        binding.tvEmpty.isVisible = filtered.isEmpty()
        binding.rvTasks.isVisible = filtered.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
