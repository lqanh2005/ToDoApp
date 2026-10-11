package com.lqanh.todoandroid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
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
import com.lqanh.todoandroid.ui.PriorityStyle
import com.lqanh.todoandroid.ui.TaskViewModel
import com.lqanh.todoandroid.ui.adapters.GroupedTaskAdapter
import com.lqanh.todoandroid.ui.adapters.TaskSection
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

// Màn hình tab Công việc: tiến độ hôm nay, lọc theo danh mục, tìm kiếm và danh sách task theo nhóm.
class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()

    private var category = "all"
    private var priorityFilter: PriorityLevel? = null
    private var sortMode = SORT_PRIORITY_HIGH
    private val expandedSections = mutableMapOf(
        TaskSection.PREVIOUS to true,
        TaskSection.TODAY to true,
        TaskSection.FUTURE to true,
        TaskSection.COMPLETE to true
    )

    private val adapter by lazy {
        GroupedTaskAdapter(
            onToggleComplete = { viewModel.toggleTaskComplete(it) },
            onDelete = { viewModel.deleteTask(it) },
            onEditSchedule = { showScheduleEditor(it) },
            onOpenDetail = { viewModel.openTaskDetail(it.id) },
            onToggleStar = { viewModel.toggleStar(it) },
            onFocus = { showFocusTimer(it) },
            onToggleSection = { section ->
                expandedSections[section] = !(expandedSections[section] ?: true)
                refreshList()
            }
        )
    }

    private var editingScheduleTaskId: String? = null

    // Tạo view từ layout fragment_tasks.xml.
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Gắn adapter, sự kiện lọc/tìm kiếm, nhận kết quả sửa lịch và lắng nghe dữ liệu từ ViewModel.
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

        binding.btnFilterPriority.setOnClickListener { showPriorityFilterMenu() }
        binding.btnSort.setOnClickListener { showSortMenu() }
        binding.tvActiveFilter.setOnClickListener {
            priorityFilter = null
            updateFilterIndicators()
            refreshList()
        }
        updateFilterIndicators()

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

    // Mở hộp thoại chọn ngày/giờ để sửa lịch của task.
    private fun showScheduleEditor(task: Task) {
        editingScheduleTaskId = task.id
        if (childFragmentManager.findFragmentByTag(DateScheduleDialogFragment.TAG) != null) return
        DateScheduleDialogFragment
            .forTask(task)
            .show(childFragmentManager, DateScheduleDialogFragment.TAG)
    }

    // Mở hộp thoại đếm giờ tập trung cho task.
    private fun showFocusTimer(task: Task) {
        if (childFragmentManager.findFragmentByTag(FocusTimerDialogFragment.TAG) != null) return
        FocusTimerDialogFragment.newInstance(task.id, task.title)
            .show(childFragmentManager, FocusTimerDialogFragment.TAG)
    }

    // Tạo nhãn hạn chót dạng "HH:mm · today/Tomorrow/MMM d".
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

    // Cập nhật số lượng task trên từng chip danh mục.
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

    // Menu lọc theo mức ưu tiên: All, High, Medium, Low.
    private fun showPriorityFilterMenu() {
        PopupMenu(requireContext(), binding.btnFilterPriority).apply {
            menu.add(0, FILTER_ALL, 0, "All priorities").apply {
                isCheckable = true
                isChecked = priorityFilter == null
            }
            PriorityStyle.ordered.forEachIndexed { index, level ->
                menu.add(0, index, index + 1, PriorityStyle.label(level)).apply {
                    isCheckable = true
                    isChecked = priorityFilter == level
                }
            }
            menu.setGroupCheckable(0, true, true)
            setOnMenuItemClickListener { item ->
                priorityFilter = if (item.itemId == FILTER_ALL) null else PriorityStyle.ordered[item.itemId]
                updateFilterIndicators()
                refreshList()
                true
            }
            show()
        }
    }

    // Menu chọn cách sắp xếp danh sách.
    private fun showSortMenu() {
        PopupMenu(requireContext(), binding.btnSort).apply {
            SORT_LABELS.forEachIndexed { index, label ->
                menu.add(0, index, index, label).apply {
                    isCheckable = true
                    isChecked = sortMode == index
                }
            }
            menu.setGroupCheckable(0, true, true)
            setOnMenuItemClickListener { item ->
                sortMode = item.itemId
                updateFilterIndicators()
                refreshList()
                true
            }
            show()
        }
    }

    // Tô màu nút lọc theo mức đang lọc và hiện nhãn bộ lọc/sắp xếp đang áp dụng (bấm nhãn để bỏ lọc).
    private fun updateFilterIndicators() {
        val filter = priorityFilter
        val idle = ContextCompat.getColor(requireContext(), R.color.slate_500)
        binding.btnFilterPriority.setColorFilter(filter?.let { PriorityStyle.color(it) } ?: idle)
        binding.btnSort.setColorFilter(
            if (sortMode == SORT_PRIORITY_HIGH) idle
            else ContextCompat.getColor(requireContext(), R.color.primary)
        )
        binding.tvActiveFilter.isVisible = filter != null
        if (filter != null) {
            binding.tvActiveFilter.text = "${PriorityStyle.label(filter)} priority  ✕"
            binding.tvActiveFilter.setTextColor(PriorityStyle.color(filter))
        }
    }

    // Lọc theo danh mục + mức ưu tiên + từ khóa, sắp xếp, chia nhóm rồi đưa vào adapter.
    private fun refreshList(
        tasks: List<Task> = viewModel.uiState.value.tasks,
        query: String = viewModel.uiState.value.searchQuery
    ) {
        val filtered = tasks
            .filter { category == "all" || it.category == category }
            .filter { priorityFilter == null || it.priority == priorityFilter }
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
                    SORT_PRIORITY_LOW -> compareBy<Task> { PriorityStyle.score(it.priority) }
                        .thenBy(nullsLast()) { TaskDateUtils.resolveStartAtMillis(it) }
                    SORT_DUE_TIME -> compareBy<Task, Long?>(nullsLast()) {
                        TaskDateUtils.resolveStartAtMillis(it) ?: TaskDateUtils.resolveDueDayMillis(it)
                    }
                    SORT_NEWEST -> compareByDescending { it.id }
                    SORT_NAME -> compareBy { it.title.lowercase() }
                    else -> compareByDescending<Task> { PriorityStyle.score(it.priority) }
                        .thenBy(nullsLast()) { TaskDateUtils.resolveStartAtMillis(it) }
                }
            )

        val items = GroupedTaskAdapter.buildItems(filtered, expandedSections)
        adapter.submitList(items)
        binding.tvEmpty.isVisible = filtered.isEmpty()
        binding.rvTasks.isVisible = filtered.isNotEmpty()
    }

    // Giải phóng binding khi view bị hủy để tránh rò rỉ bộ nhớ.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val FILTER_ALL = 100

        private const val SORT_PRIORITY_HIGH = 0
        private const val SORT_PRIORITY_LOW = 1
        private const val SORT_DUE_TIME = 2
        private const val SORT_NEWEST = 3
        private const val SORT_NAME = 4

        private val SORT_LABELS = listOf(
            "Priority: High → Low",
            "Priority: Low → High",
            "Due time",
            "Newest first",
            "Name A–Z"
        )
    }
}
