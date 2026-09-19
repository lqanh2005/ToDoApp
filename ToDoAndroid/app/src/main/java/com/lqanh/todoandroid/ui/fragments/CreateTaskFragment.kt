package com.lqanh.todoandroid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.lqanh.todoandroid.data.PriorityLevel
import com.lqanh.todoandroid.data.Task
import com.lqanh.todoandroid.data.TaskStatus
import com.lqanh.todoandroid.databinding.FragmentCreateTaskBinding
import com.lqanh.todoandroid.ui.TaskViewModel
import kotlin.random.Random

class CreateTaskFragment : Fragment() {

    private var _binding: FragmentCreateTaskBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()

    private var timeSlot = "14:30"
    private val categories = listOf(
        Triple("work", "Work", "💼"),
        Triple("personal", "Personal", "👤"),
        Triple("study", "Study", "📚"),
        Triple("shopping", "Shopping", "🛒")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvTime.text = timeSlot
        binding.spinnerCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories.map { "${it.third} ${it.second}" }
        )

        binding.btnPickTime.setOnClickListener { showTimePicker() }
        binding.btnSave.setOnClickListener { save() }
    }

    private fun showTimePicker() {
        val hour = timeSlot.substringBefore(":").toIntOrNull() ?: 14
        val minute = timeSlot.substringAfter(":").toIntOrNull() ?: 30
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText("Pick a time")
            .build()
        picker.addOnPositiveButtonClickListener {
            timeSlot = "%02d:%02d".format(picker.hour, picker.minute)
            binding.tvTime.text = timeSlot
        }
        picker.show(parentFragmentManager, "time_picker")
    }

    private fun save() {
        val title = binding.etTitle.text?.toString().orEmpty().trim()
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a task name", Toast.LENGTH_SHORT).show()
            return
        }
        val priority = when (binding.rgPriority.checkedRadioButtonId) {
            binding.rbTb.id -> PriorityLevel.TB
            binding.rbThap.id -> PriorityLevel.THAP
            else -> PriorityLevel.CAO
        }
        val dueKey = when (binding.rgDue.checkedRadioButtonId) {
            binding.rbTomorrow.id -> "tomorrow"
            binding.rbWeekend.id -> "weekend"
            else -> "today"
        }
        val dueLabel = when (dueKey) {
            "tomorrow" -> "Tomorrow"
            "weekend" -> "This weekend"
            else -> "Today"
        }
        val cat = categories.getOrElse(binding.spinnerCategory.selectedItemPosition) { categories[0] }
        val task = Task(
            id = "task-${System.currentTimeMillis()}",
            code = "#${Random.nextInt(100, 999)}",
            title = title,
            description = binding.etDescription.text?.toString().orEmpty().trim(),
            status = TaskStatus.TODO,
            priority = priority,
            category = cat.first,
            categoryLabel = cat.second,
            categoryEmoji = cat.third,
            dueDate = dueLabel,
            dueTime = "$timeSlot (${if (dueKey == "today") "Today" else "Upcoming"})",
            reminder = "15",
            subtasks = emptyList()
        )
        viewModel.createTask(task)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
