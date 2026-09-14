package com.lqanh.todoandroid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lqanh.todoandroid.data.TaskStatus
import com.lqanh.todoandroid.databinding.FragmentScheduleBinding
import com.lqanh.todoandroid.ui.TaskViewModel
import kotlinx.coroutines.launch

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val tasks = state.tasks
                    val done = tasks.count { it.status == TaskStatus.DONE }
                    val todo = tasks.count { it.status == TaskStatus.TODO }
                    val overdue = tasks.count { it.isOverdue && it.status != TaskStatus.DONE }
                    val percent = if (tasks.isEmpty()) 0 else done * 100 / tasks.size

                    binding.tvSchedulePercent.text = "$percent%"
                    binding.tvDoneCount.text = "$done\nĐã xong"
                    binding.tvOverdueCount.text = "$overdue\nQuá hạn"
                    binding.tvTodoCount.text = "$todo\nSắp tới"

                    binding.tvKanbanTodo.text = "Cần làm ($todo)\n" +
                        tasks.filter { it.status == TaskStatus.TODO }.take(5)
                            .joinToString("\n") { "• ${it.title}" }
                    binding.tvKanbanDoing.text = "Đang làm (${tasks.count { it.status == TaskStatus.DOING }})\n" +
                        tasks.filter { it.status == TaskStatus.DOING }
                            .joinToString("\n") { "• ${it.title}" }
                    binding.tvKanbanDone.text = "Hoàn thành ($done)\n" +
                        tasks.filter { it.status == TaskStatus.DONE }.take(5)
                            .joinToString("\n") { "• ${it.title}" }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
