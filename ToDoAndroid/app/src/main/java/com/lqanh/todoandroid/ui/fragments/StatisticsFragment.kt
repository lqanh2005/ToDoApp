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
import com.lqanh.todoandroid.databinding.FragmentStatisticsBinding
import com.lqanh.todoandroid.ui.TaskViewModel
import kotlinx.coroutines.launch

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val tasks = state.tasks
                    val total = tasks.size
                    val done = tasks.count { it.status == TaskStatus.DONE }
                    val doing = tasks.count { it.status == TaskStatus.DOING }
                    val todo = tasks.count { it.status == TaskStatus.TODO }
                    val overdue = tasks.count { it.isOverdue && it.status != TaskStatus.DONE }
                    val rate = if (total == 0) 0 else done * 100 / total

                    binding.tvRate.text = "$rate% hoàn thành — đã xử lý $done/$total nhiệm vụ"
                    binding.tvStatDone.text = "$done\nĐã xong"
                    binding.tvStatDoing.text = "$doing\nĐang làm"
                    binding.tvStatTodo.text = "$todo\nCần làm"
                    binding.tvStatOverdue.text = "$overdue\nTrễ hạn"

                    fun pct(count: Int) = if (total == 0) 0 else count * 100 / total
                    val work = tasks.count { it.category == "work" }
                    val personal = tasks.count { it.category == "personal" }
                    val study = tasks.count { it.category == "study" }
                    val shopping = tasks.count { it.category == "shopping" }
                    binding.tvCategoryBreakdown.text =
                        "💼 Công việc ($work) — ${pct(work)}%\n" +
                            "👤 Cá nhân ($personal) — ${pct(personal)}%\n" +
                            "📚 Học tập ($study) — ${pct(study)}%\n" +
                            "🛒 Mua sắm ($shopping) — ${pct(shopping)}%"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
