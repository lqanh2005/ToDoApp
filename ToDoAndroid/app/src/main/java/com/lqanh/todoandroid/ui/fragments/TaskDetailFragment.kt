package com.lqanh.todoandroid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lqanh.todoandroid.R
import com.lqanh.todoandroid.data.PriorityLevel
import com.lqanh.todoandroid.data.TaskStatus
import com.lqanh.todoandroid.databinding.FragmentTaskDetailBinding
import com.lqanh.todoandroid.ui.TaskViewModel
import kotlinx.coroutines.launch

class TaskDetailFragment : Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val task = state.selectedTask ?: return@collect
                    bindTask(task.id)
                }
            }
        }
    }

    private fun bindTask(taskId: String) {
        val task = viewModel.uiState.value.tasks.find { it.id == taskId } ?: return
        binding.tvCode.text = "${task.code} • ${task.sprint ?: "Sprint"}"
        binding.tvDetailTitle.text = task.title
        binding.tvDetailDesc.text = task.description.orEmpty()
        binding.tvDetailDue.text = "Hạn chót: ${task.dueTime} • ${task.dueDate}"

        val priority = when (task.priority) {
            PriorityLevel.CAO -> "Ưu tiên Cao"
            PriorityLevel.TB -> "Ưu tiên Vừa"
            PriorityLevel.THAP -> "Ưu tiên Thấp"
        }
        binding.tvDetailBadges.text =
            "${task.categoryEmoji.orEmpty()} ${task.categoryLabel} • $priority • Nhắc trước ${task.reminder}p"

        when (task.status) {
            TaskStatus.TODO -> binding.statusGroup.check(R.id.btnTodo)
            TaskStatus.DOING -> binding.statusGroup.check(R.id.btnDoing)
            TaskStatus.DONE -> binding.statusGroup.check(R.id.btnDone)
        }

        binding.statusGroup.clearOnButtonCheckedListeners()
        binding.statusGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val status = when (checkedId) {
                R.id.btnDoing -> TaskStatus.DOING
                R.id.btnDone -> TaskStatus.DONE
                else -> TaskStatus.TODO
            }
            if (status != task.status) {
                viewModel.updateTask(task.copy(status = status))
            }
        }

        binding.subtasksContainer.removeAllViews()
        task.subtasks.forEachIndexed { index, sub ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(24, 20, 24, 20)
                setBackgroundResource(R.drawable.bg_card_low)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.topMargin = 8
                layoutParams = lp
            }
            val cb = CheckBox(requireContext()).apply {
                isChecked = sub.completed
                buttonTintList = ContextCompat.getColorStateList(requireContext(), R.color.primary)
                setOnCheckedChangeListener { _, checked ->
                    val updated = task.subtasks.toMutableList()
                    updated[index] = sub.copy(completed = checked)
                    val allDone = updated.isNotEmpty() && updated.all { it.completed }
                    viewModel.updateTask(
                        task.copy(
                            subtasks = updated,
                            status = if (allDone) TaskStatus.DONE else task.status
                        )
                    )
                }
            }
            val tv = TextView(requireContext()).apply {
                text = sub.title
                textSize = 13f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            row.addView(cb)
            row.addView(tv)
            binding.subtasksContainer.addView(row)
        }

        binding.btnMarkDone.text = if (task.status == TaskStatus.DONE) {
            "Chuyển về Chưa xong"
        } else {
            "Đánh dấu hoàn thành"
        }
        binding.btnMarkDone.setOnClickListener {
            val next = if (task.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE
            viewModel.updateTask(task.copy(status = next))
        }

        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Xóa công việc này?")
                .setMessage("Bạn có chắc muốn xóa \"${task.title}\"?")
                .setPositiveButton("Xóa") { _, _ -> viewModel.deleteTask(task.id) }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
