package com.lqanh.todoandroid.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.lqanh.todoandroid.R
import com.lqanh.todoandroid.data.Task
import com.lqanh.todoandroid.data.TaskDateUtils
import com.lqanh.todoandroid.data.TaskStatus
import com.lqanh.todoandroid.databinding.FragmentScheduleBinding
import com.lqanh.todoandroid.ui.TaskViewModel
import com.lqanh.todoandroid.ui.adapters.TaskAdapter
import androidx.fragment.app.setFragmentResultListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()

    private val displayMonth: Calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    private var selectedDayMillis: Long = TaskDateUtils.startOfDay()
    private var allTasks: List<Task> = emptyList()
    private var daysWithTasks: Set<Long> = emptySet()

    private val adapter by lazy {
        TaskAdapter(
            onToggleComplete = { viewModel.toggleTaskComplete(it) },
            onChangeStatus = { id, status ->
                if (status == TaskStatus.TODO) viewModel.resolveCompletion(id, false)
                else viewModel.changeTaskStatus(id, status)
            },
            onDelete = { viewModel.deleteTask(it) },
            onAskCompletion = { task ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Hết thời gian task")
                    .setMessage("\"${task.title}\" đã hết thời gian.\nBạn đã hoàn thành chưa?")
                    .setPositiveButton("Đã xong") { _, _ -> viewModel.resolveCompletion(task.id, true) }
                    .setNegativeButton("Chưa xong") { _, _ -> viewModel.resolveCompletion(task.id, false) }
                    .setNeutralButton("Để sau", null)
                    .show()
            },
            onEditSchedule = { task ->
                editingScheduleTaskId = task.id
                if (childFragmentManager.findFragmentByTag(DateScheduleDialogFragment.TAG) != null) return@TaskAdapter
                val initial = task.dueAtMillis
                    ?: TaskDateUtils.resolveDueDayMillis(task)
                    ?: System.currentTimeMillis()
                DateScheduleDialogFragment.newInstance(initial)
                    .show(childFragmentManager, DateScheduleDialogFragment.TAG)
            }
        )
    }

    private var editingScheduleTaskId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvDayTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDayTasks.adapter = adapter

        setFragmentResultListener(DateScheduleDialogFragment.REQUEST_KEY) { _, bundle ->
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
            val dueLabel = formatScheduleLabel(dueMillis, hasTime, hour, minute)
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

        binding.btnPrevMonth.setOnClickListener {
            displayMonth.add(Calendar.MONTH, -1)
            renderCalendar()
        }
        binding.btnNextMonth.setOnClickListener {
            displayMonth.add(Calendar.MONTH, 1)
            renderCalendar()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    allTasks = state.tasks
                    daysWithTasks = TaskDateUtils.daysWithTasks(state.tasks)
                    renderCalendar()
                    renderDayTasks()
                }
            }
        }
    }

    private fun renderCalendar() {
        binding.tvMonthYear.text = SimpleDateFormat("MMMM yyyy", Locale("vi")).format(displayMonth.time)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("vi")) else it.toString() }

        binding.gridDays.removeAllViews()
        val first = (displayMonth.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            timeInMillis = TaskDateUtils.startOfDay(timeInMillis)
        }
        val startOffset = first.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
        val cursor = (first.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -startOffset) }

        repeat(42) {
            val dayCal = cursor.clone() as Calendar
            val dayMillis = TaskDateUtils.startOfDay(dayCal.timeInMillis)
            val row = layoutInflater.inflate(R.layout.item_schedule_day, binding.gridDays, false)
            val tvDay = row.findViewById<TextView>(R.id.tvDay)
            val dot = row.findViewById<View>(R.id.dotTask)

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
            row.layoutParams = params

            val inMonth = dayCal.get(Calendar.MONTH) == displayMonth.get(Calendar.MONTH)
            val hasTasks = daysWithTasks.any { TaskDateUtils.isSameDay(it, dayMillis) }
            val selected = TaskDateUtils.isSameDay(dayMillis, selectedDayMillis)

            tvDay.text = dayCal.get(Calendar.DAY_OF_MONTH).toString()
            dot.visibility = if (hasTasks && inMonth) View.VISIBLE else View.INVISIBLE

            when {
                selected -> {
                    tvDay.setBackgroundResource(R.drawable.bg_day_selected)
                    tvDay.setTextColor(Color.WHITE)
                    dot.visibility = if (hasTasks) View.VISIBLE else View.INVISIBLE
                }
                hasTasks && inMonth -> {
                    tvDay.background = null
                    tvDay.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                }
                inMonth -> {
                    tvDay.background = null
                    tvDay.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_700))
                }
                else -> {
                    tvDay.background = null
                    tvDay.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_400))
                    dot.visibility = View.INVISIBLE
                }
            }

            row.setOnClickListener {
                selectedDayMillis = dayMillis
                if (!inMonth) {
                    displayMonth.timeInMillis = dayMillis
                    displayMonth.set(Calendar.DAY_OF_MONTH, 1)
                    displayMonth.timeInMillis = TaskDateUtils.startOfDay(displayMonth.timeInMillis)
                }
                renderCalendar()
                renderDayTasks()
            }

            binding.gridDays.addView(row)
            cursor.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    private fun renderDayTasks() {
        val dayTasks = TaskDateUtils.tasksOnDay(allTasks, selectedDayMillis)
        binding.tvSelectedDate.text = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale("vi"))
            .format(selectedDayMillis)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("vi")) else it.toString() }

        adapter.submitList(dayTasks)
        binding.rvDayTasks.isVisible = dayTasks.isNotEmpty()
        binding.tvEmptyDay.isVisible = dayTasks.isEmpty()
    }

    private fun formatScheduleLabel(
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
            today.timeInMillis -> "hôm nay"
            tomorrow.timeInMillis -> "Ngày mai"
            else -> SimpleDateFormat("dd 'Th'MM", Locale("vi")).format(Date(millis))
        }
        return if (withTime) "%02d:%02d · %s".format(hour, minute, datePart) else datePart
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
