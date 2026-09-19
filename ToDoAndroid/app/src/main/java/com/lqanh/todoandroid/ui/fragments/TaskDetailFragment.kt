package com.lqanh.todoandroid.ui.fragments

import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lqanh.todoandroid.R
import com.lqanh.todoandroid.data.Subtask
import com.lqanh.todoandroid.data.Task
import com.lqanh.todoandroid.data.TaskDateUtils
import com.lqanh.todoandroid.databinding.FragmentTaskDetailBinding
import com.lqanh.todoandroid.databinding.ItemDetailSubtaskBinding
import com.lqanh.todoandroid.ui.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import kotlinx.coroutines.launch

class TaskDetailFragment : Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()

    private var taskId: String = ""
    private var current: Task? = null
    private var updatingUi = false
    private var categoryPopup: PopupWindow? = null

    private val categories = mutableListOf(
        Triple("work", "Work", "💼"),
        Triple("personal", "Personal", "👤"),
        Triple("study", "Study", "📚"),
        Triple("shopping", "Shopping", "🛒")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskId = requireArguments().getString(ARG_TASK_ID).orEmpty()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnBack.setOnClickListener { viewModel.closeChild() }
        binding.btnMore.setOnClickListener { showMoreMenu() }
        binding.btnCategory.setOnClickListener { showCategoryMenu() }
        binding.btnAddSubtask.setOnClickListener { promptAddSubtask() }
        binding.rowDueDate.setOnClickListener { openScheduleEditor() }
        binding.rowTime.setOnClickListener { openScheduleEditor() }
        binding.rowRepeat.setOnClickListener {
            Toast.makeText(requireContext(), "Repeat coming soon", Toast.LENGTH_SHORT).show()
        }
        binding.rowNotes.setOnClickListener { promptNotes() }
        binding.rowAttachment.setOnClickListener {
            Toast.makeText(requireContext(), "Attachments coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.etTitle.doAfterTextChanged {
            if (updatingUi) return@doAfterTextChanged
            val task = current ?: return@doAfterTextChanged
            val title = it?.toString().orEmpty().trim()
            if (title.isNotEmpty() && title != task.title) {
                viewModel.updateTask(task.copy(title = title))
            }
        }

        childFragmentManager.setFragmentResultListener(
            DateScheduleDialogFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val task = current ?: return@setFragmentResultListener
            val noDate = bundle.getBoolean(DateScheduleDialogFragment.KEY_NO_DATE)
            val dueAt = if (noDate) null else bundle.getLong(DateScheduleDialogFragment.KEY_MILLIS)
            val hasTime = bundle.getBoolean(DateScheduleDialogFragment.KEY_HAS_TIME)
            val hour = bundle.getInt(DateScheduleDialogFragment.KEY_HOUR)
            val minute = bundle.getInt(DateScheduleDialogFragment.KEY_MINUTE)
            val rem = bundle.getString(DateScheduleDialogFragment.KEY_REMINDER).orEmpty()
            val reminderValue = when {
                rem.isBlank() || rem.equals("No", true) -> "0"
                rem.contains(":") -> "5"
                else -> rem.filter { it.isDigit() }.ifBlank { "15" }
            }
            val dueLabel = formatDueLabel(
                if (noDate) null else dueAt,
                hasTime,
                hour,
                minute
            )
            val (start, end) = TaskDateUtils.buildStartEnd(
                if (noDate) null else dueAt,
                hasTime,
                hour,
                minute
            )
            viewModel.updateTask(
                task.copy(
                    dueDate = dueLabel,
                    dueTime = dueLabel,
                    dueAtMillis = dueAt?.let { TaskDateUtils.startOfDay(it) },
                    startAtMillis = start,
                    endAtMillis = end,
                    reminder = reminderValue,
                    awaitingCompletion = false,
                    isOverdue = false
                )
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val task = state.tasks.find { it.id == taskId }
                    if (task == null) {
                        viewModel.closeChild()
                        return@collect
                    }
                    bindTask(task)
                }
            }
        }
    }

    private fun bindTask(task: Task) {
        current = task
        updatingUi = true
        if (binding.etTitle.text?.toString() != task.title) {
            binding.etTitle.setText(task.title)
            binding.etTitle.setSelection(binding.etTitle.text?.length ?: 0)
        }
        binding.btnCategory.text = task.categoryLabel.ifBlank { "Work" }

        bindSubtasks(task)
        bindSchedule(task)

        val notes = task.notes?.trim().orEmpty()
        if (notes.isBlank()) {
            binding.tvNotes.text = "Add"
            binding.tvNotes.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_400))
        } else {
            binding.tvNotes.text = notes
            binding.tvNotes.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_700))
        }

        val attachments = task.attachmentsCount ?: 0
        if (attachments <= 0) {
            binding.tvAttachment.text = "Add"
            binding.tvAttachment.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_400))
        } else {
            binding.tvAttachment.text = "$attachments file(s)"
            binding.tvAttachment.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_700))
        }
        updatingUi = false
    }

    private fun bindSubtasks(task: Task) {
        binding.subtaskContainer.removeAllViews()
        task.subtasks.forEach { sub ->
            val row = ItemDetailSubtaskBinding.inflate(layoutInflater, binding.subtaskContainer, false)
            row.tvSubTitle.text = sub.title
            row.tvSubTitle.paintFlags = if (sub.completed) {
                row.tvSubTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                row.tvSubTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            row.tvSubTitle.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (sub.completed) R.color.slate_400 else R.color.on_surface
                )
            )
            if (sub.completed) {
                row.vSubCheckBg.setBackgroundResource(R.drawable.bg_checkbox_checked)
                row.ivSubCheck.isVisible = true
            } else {
                row.vSubCheckBg.setBackgroundResource(R.drawable.bg_checkbox_unchecked)
                row.ivSubCheck.isVisible = false
            }
            row.btnSubCheckbox.setOnClickListener {
                val updated = task.subtasks.map {
                    if (it.id == sub.id) it.copy(completed = !it.completed) else it
                }
                viewModel.updateTask(task.copy(subtasks = updated))
            }
            binding.subtaskContainer.addView(row.root)
        }
    }

    private fun bindSchedule(task: Task) {
        val day = TaskDateUtils.resolveDueDayMillis(task)
        binding.tvDueDate.text = if (day == null) {
            "No"
        } else {
            SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date(day))
        }

        val time = extractTime(task)
        if (time == null) {
            binding.tvTime.text = "No"
            binding.reminderDetails.isVisible = false
        } else {
            binding.tvTime.text = time
            val hasReminder = task.reminder.isNotBlank() && task.reminder != "0"
            binding.reminderDetails.isVisible = hasReminder
            if (hasReminder) {
                binding.tvReminderAt.text = reminderAtLabel(time, task.reminder)
                binding.tvReminderType.text = "Standard notification"
            }
        }
    }

    private fun reminderAtLabel(time: String, reminder: String): String {
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return time
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return time
        val offset = reminder.filter { it.isDigit() }.toIntOrNull() ?: 5
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            add(Calendar.MINUTE, -offset)
        }
        return "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    }

    private fun extractTime(task: Task): String? {
        TaskDateUtils.resolveStartAtMillis(task)?.let {
            val cal = Calendar.getInstance().apply { timeInMillis = it }
            return "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        }
        val matcher = TIME_PATTERN.matcher("${task.dueTime} ${task.dueDate}")
        if (matcher.find()) {
            val h = matcher.group(1)?.toIntOrNull() ?: return null
            val m = matcher.group(2)?.toIntOrNull() ?: return null
            return "%02d:%02d".format(h, m)
        }
        return null
    }

    private fun openScheduleEditor() {
        val task = current ?: return
        if (childFragmentManager.findFragmentByTag(DateScheduleDialogFragment.TAG) != null) return
        val initial = task.dueAtMillis
            ?: TaskDateUtils.resolveDueDayMillis(task)
            ?: System.currentTimeMillis()
        DateScheduleDialogFragment.newInstance(initial)
            .show(childFragmentManager, DateScheduleDialogFragment.TAG)
    }

    private fun showMoreMenu() {
        PopupMenu(requireContext(), binding.btnMore).apply {
            menu.add(0, 1, 0, "Delete task")
            setOnMenuItemClickListener { item ->
                if (item.itemId == 1) {
                    viewModel.deleteTask(taskId)
                    viewModel.closeChild()
                }
                true
            }
            show()
        }
    }

    private fun showCategoryMenu() {
        categoryPopup?.dismiss()
        val content = layoutInflater.inflate(R.layout.popup_category_menu, null)
        val list = content.findViewById<LinearLayout>(R.id.categoryList)
        val btnAddTag = content.findViewById<TextView>(R.id.btnAddTag)
        btnAddTag.isVisible = false

        val popup = PopupWindow(
            content,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            elevation = 12f
            isOutsideTouchable = true
            setBackgroundDrawable(
                ContextCompat.getDrawable(requireContext(), android.R.color.transparent)
            )
        }
        categoryPopup = popup

        list.removeAllViews()
        categories.forEachIndexed { index, cat ->
            val row = layoutInflater.inflate(R.layout.item_category_option, list, false) as TextView
            row.text = cat.second
            row.setOnClickListener {
                val task = current ?: return@setOnClickListener
                viewModel.updateTask(
                    task.copy(
                        category = cat.first,
                        categoryLabel = cat.second,
                        categoryEmoji = cat.third
                    )
                )
                popup.dismiss()
            }
            list.addView(row)
            if (index < categories.lastIndex) {
                list.addView(View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                    ).also { it.setMargins(0, 0, 0, 0) }
                    setBackgroundColor(0xFFE8EDF4.toInt())
                })
            }
        }

        content.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        popup.showAsDropDown(binding.btnCategory, 0, 8, Gravity.START)
    }

    private fun promptAddSubtask() {
        val input = EditText(requireContext()).apply {
            hint = "Sub-task name"
            setPadding(48, 32, 48, 32)
            setSingleLine(true)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Add Sub-task")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text?.toString()?.trim().orEmpty()
                if (name.isEmpty()) return@setPositiveButton
                val task = current ?: return@setPositiveButton
                val sub = Subtask("sub-${System.currentTimeMillis()}", name, false)
                viewModel.updateTask(task.copy(subtasks = task.subtasks + sub))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun promptNotes() {
        val task = current ?: return
        val input = EditText(requireContext()).apply {
            hint = "Notes"
            setText(task.notes.orEmpty())
            setPadding(48, 32, 48, 32)
            minLines = 3
            gravity = Gravity.TOP
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Notes")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                viewModel.updateTask(task.copy(notes = input.text?.toString()?.trim().orEmpty()))
            }
            .setNegativeButton("Cancel", null)
            .show()
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

    override fun onDestroyView() {
        categoryPopup?.dismiss()
        categoryPopup = null
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "TaskDetailFragment"
        private const val ARG_TASK_ID = "task_id"
        private val TIME_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{2})")

        fun newInstance(taskId: String) = TaskDetailFragment().apply {
            arguments = Bundle().apply { putString(ARG_TASK_ID, taskId) }
        }
    }
}
