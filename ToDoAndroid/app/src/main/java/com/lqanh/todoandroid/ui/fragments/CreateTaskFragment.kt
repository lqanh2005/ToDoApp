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
import com.lqanh.todoandroid.data.TaskDateUtils
import com.lqanh.todoandroid.data.TaskStatus
import com.lqanh.todoandroid.databinding.FragmentCreateTaskBinding
import com.lqanh.todoandroid.ui.TaskViewModel
import kotlin.random.Random

// Màn hình tạo task dạng form đầy đủ (tiêu đề, mô tả, ưu tiên, hạn, giờ, danh mục).
class CreateTaskFragment : Fragment() {

    private var _binding: FragmentCreateTaskBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()

    private var timeSlot = "14:30"
    private val categories = listOf(
        "work" to "Work",
        "personal" to "Personal",
        "study" to "Study",
        "shopping" to "Shopping"
    )

    // Tạo view từ layout fragment_create_task.xml.
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Hiển thị giờ mặc định, nạp danh mục vào spinner và gắn sự kiện nút.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvTime.text = TaskDateUtils.formatClock(14, 30)
        binding.spinnerCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories.map { it.second }
        )

        binding.btnPickTime.setOnClickListener { showTimePicker() }
        binding.btnSave.setOnClickListener { save() }
    }

    // Mở đồng hồ chọn giờ (12h/24h theo Cài đặt).
    private fun showTimePicker() {
        val hour = timeSlot.substringBefore(":").toIntOrNull() ?: 14
        val minute = timeSlot.substringAfter(":").toIntOrNull() ?: 30
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(if (TaskDateUtils.use24HourClock) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText("Pick a time")
            .build()
        picker.addOnPositiveButtonClickListener {
            timeSlot = "%02d:%02d".format(picker.hour, picker.minute)
            binding.tvTime.text = TaskDateUtils.formatClock(picker.hour, picker.minute)
        }
        picker.show(parentFragmentManager, "time_picker")
    }

    // Kiểm tra tiêu đề, tạo Task từ dữ liệu form và gửi cho ViewModel.
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
            dueDate = dueLabel,
            dueTime = "$timeSlot (${if (dueKey == "today") "Today" else "Upcoming"})",
            reminder = "15",
            subtasks = emptyList()
        )
        viewModel.createTask(task)
    }

    // Giải phóng binding khi view bị hủy.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
