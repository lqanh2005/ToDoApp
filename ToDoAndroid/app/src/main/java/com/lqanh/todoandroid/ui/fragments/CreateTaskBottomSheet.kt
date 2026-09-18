package com.lqanh.todoandroid.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lqanh.todoandroid.R
import com.lqanh.todoandroid.data.PriorityLevel
import com.lqanh.todoandroid.data.Subtask
import com.lqanh.todoandroid.data.Task
import com.lqanh.todoandroid.data.TaskDateUtils
import com.lqanh.todoandroid.data.TaskStatus
import com.lqanh.todoandroid.databinding.BottomSheetCreateTaskBinding
import com.lqanh.todoandroid.ui.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class CreateTaskBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCreateTaskBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()

    private val categories = listOf(
        Triple("none", "No Category", ""),
        Triple("work", "Công việc", "💼"),
        Triple("personal", "Cá nhân", "👤"),
        Triple("study", "Học tập", "📚"),
        Triple("shopping", "Mua sắm", "🛒")
    )

    private var categoryIndex = 0
    private var dueMillis: Long? = System.currentTimeMillis()
    private var hasTime = false
    private var timeHour = 0
    private var timeMinute = 0
    private var reminderValue = "15"
    private val subtaskInputs = mutableListOf<EditText>()

    override fun getTheme(): Int = R.style.ThemeOverlay_ToDo_BottomSheetDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            val sheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            sheet?.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            sheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
            binding.etTitle.requestFocus()
            binding.etTitle.post {
                val imm = requireContext().getSystemService(InputMethodManager::class.java)
                imm?.showSoftInput(binding.etTitle, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetCreateTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateDayLabel()
        updateCategoryLabel()
        updateSubmitEnabled()

        setFragmentResultListener(DateScheduleDialogFragment.REQUEST_KEY) { _, bundle ->
            val noDate = bundle.getBoolean(DateScheduleDialogFragment.KEY_NO_DATE)
            if (noDate) {
                dueMillis = null
                hasTime = false
            } else {
                dueMillis = bundle.getLong(DateScheduleDialogFragment.KEY_MILLIS)
                hasTime = bundle.getBoolean(DateScheduleDialogFragment.KEY_HAS_TIME)
                if (hasTime) {
                    timeHour = bundle.getInt(DateScheduleDialogFragment.KEY_HOUR)
                    timeMinute = bundle.getInt(DateScheduleDialogFragment.KEY_MINUTE)
                }
            }
            val rem = bundle.getString(DateScheduleDialogFragment.KEY_REMINDER).orEmpty()
            reminderValue = when {
                rem.isBlank() -> "0"
                rem.contains("At task", true) || rem.equals("On time", true) -> "0"
                rem.startsWith("5") -> "5"
                rem.startsWith("15") -> "15"
                rem.startsWith("30") -> "30"
                rem.startsWith("1 day") -> "1440"
                else -> rem
            }
            updateDayLabel()
        }

        binding.etTitle.doAfterTextChanged { updateSubmitEnabled() }
        binding.btnCategory.setOnClickListener { showCategoryMenu() }
        binding.btnDate.setOnClickListener { showDatePicker() }
        binding.btnSubtask.setOnClickListener { addInlineSubtask() }
        binding.btnMic.setOnClickListener {
            Toast.makeText(requireContext(), "Nhập giọng nói sẽ sớm có", Toast.LENGTH_SHORT).show()
        }
        binding.btnSubmit.setOnClickListener { save() }
        updateSubtaskButton()
    }

    private fun updateDayLabel() {
        val millis = dueMillis
        if (millis == null) {
            binding.tvDay.text = "—"
            binding.tvDay.isVisible = true
            return
        }
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        binding.tvDay.text = cal.get(Calendar.DAY_OF_MONTH).toString()
    }

    private fun updateCategoryLabel() {
        binding.btnCategory.text = categories[categoryIndex].second
    }

    private fun updateSubmitEnabled() {
        val enabled = binding.etTitle.text?.toString()?.trim().orEmpty().isNotEmpty()
        binding.btnSubmit.isEnabled = enabled
        binding.btnSubmit.alpha = if (enabled) 1f else 0.45f
    }

    private fun showCategoryMenu() {
        PopupMenu(requireContext(), binding.btnCategory).apply {
            categories.forEachIndexed { index, item ->
                menu.add(0, index, index, item.second)
            }
            setOnMenuItemClickListener { item ->
                categoryIndex = item.itemId
                updateCategoryLabel()
                true
            }
            show()
        }
    }

    private fun updateSubtaskButton() {
        val tint = if (subtaskInputs.isEmpty()) R.color.slate_500 else R.color.primary
        binding.btnSubtask.setColorFilter(ContextCompat.getColor(requireContext(), tint))
        binding.subtaskContainer.isVisible = subtaskInputs.isNotEmpty()
    }

    private fun addInlineSubtask() {
        val row = layoutInflater.inflate(R.layout.item_inline_subtask, binding.subtaskContainer, false)
        val et = row.findViewById<EditText>(R.id.etSubtask)
        val btnRemove = row.findViewById<ImageButton>(R.id.btnRemoveSubtask)

        subtaskInputs.add(et)
        binding.subtaskContainer.addView(row)
        updateSubtaskButton()

        btnRemove.setOnClickListener {
            binding.subtaskContainer.removeView(row)
            subtaskInputs.remove(et)
            updateSubtaskButton()
        }

        et.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                addInlineSubtask()
                true
            } else false
        }

        et.post {
            et.requestFocus()
            val imm = requireContext().getSystemService(InputMethodManager::class.java)
            imm?.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun collectSubtasks(): List<Subtask> {
        return subtaskInputs.mapIndexedNotNull { index, editText ->
            val text = editText.text?.toString()?.trim().orEmpty()
            if (text.isEmpty()) null
            else Subtask(
                id = "sub-${System.currentTimeMillis()}-$index",
                title = text
            )
        }
    }

    private fun showDatePicker() {
        if (childFragmentManager.findFragmentByTag(DateScheduleDialogFragment.TAG) != null) return
        DateScheduleDialogFragment
            .newInstance(dueMillis ?: System.currentTimeMillis())
            .show(childFragmentManager, DateScheduleDialogFragment.TAG)
    }

    private fun save() {
        val title = binding.etTitle.text?.toString().orEmpty().trim()
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập tên công việc", Toast.LENGTH_SHORT).show()
            return
        }

        val cat = categories[categoryIndex]
        val dueLabel = formatDueLabel(dueMillis, hasTime, timeHour, timeMinute)
        val category = if (cat.first == "none") "personal" else cat.first
        val categoryLabel = if (cat.first == "none") "Cá nhân" else cat.second
        val categoryEmoji = cat.third.ifBlank { "📌" }

        val task = Task(
            id = "task-${System.currentTimeMillis()}",
            code = "#${Random.nextInt(100, 999)}",
            title = title,
            description = null,
            status = TaskStatus.TODO,
            priority = PriorityLevel.TB,
            category = category,
            categoryLabel = categoryLabel,
            categoryEmoji = categoryEmoji,
            dueDate = dueLabel.ifBlank { "" },
            dueTime = dueLabel.ifBlank { "" },
            dueAtMillis = dueMillis?.let { TaskDateUtils.startOfDay(it) },
            startAtMillis = TaskDateUtils.buildStartEnd(dueMillis, hasTime, timeHour, timeMinute).first,
            endAtMillis = TaskDateUtils.buildStartEnd(dueMillis, hasTime, timeHour, timeMinute).second,
            reminder = reminderValue,
            subtasks = collectSubtasks()
        )
        viewModel.createTask(task)
        dismiss()
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
            today.timeInMillis -> "hôm nay"
            tomorrow.timeInMillis -> "Ngày mai"
            else -> SimpleDateFormat("dd 'Th'MM", Locale("vi")).format(Date(millis))
        }
        return if (withTime) {
            "%02d:%02d · %s".format(hour, minute, datePart)
        } else {
            datePart
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CreateTaskBottomSheet"
    }
}
