package com.lqanh.todoandroid.ui.adapters

import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lqanh.todoandroid.R
import com.lqanh.todoandroid.data.PriorityLevel
import com.lqanh.todoandroid.data.Task
import com.lqanh.todoandroid.data.TaskStatus
import com.lqanh.todoandroid.databinding.ItemTaskBinding

class TaskAdapter(
    private val onSelect: (Task) -> Unit,
    private val onToggleComplete: (String) -> Unit,
    private val onChangeStatus: (String, TaskStatus) -> Unit,
    private val onDelete: (String) -> Unit
) : ListAdapter<Task, TaskAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
    }

    class VH(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val task = getItem(position)
        val b = holder.binding
        val ctx = b.root.context

        b.tvTaskTitle.text = task.title
        b.tvTaskTitle.paintFlags = if (task.status == TaskStatus.DONE) {
            b.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            b.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        val priorityText = when (task.priority) {
            PriorityLevel.CAO -> "Ưu tiên Cao"
            PriorityLevel.TB -> "Ưu tiên Trung bình"
            PriorityLevel.THAP -> "Ưu tiên Thấp"
        }
        val overdue = if (task.isOverdue && task.status != TaskStatus.DONE) {
            " • ${task.overdueText ?: "Quá hạn"}"
        } else ""
        b.tvBadges.text = "$priorityText • ${task.categoryEmoji.orEmpty()} ${task.categoryLabel}$overdue"

        val doneSubs = task.subtasks.count { it.completed }
        b.tvMeta.text = buildString {
            append(task.dueTime)
            if (task.subtasks.isNotEmpty()) append(" • $doneSubs/${task.subtasks.size} mục")
            if (!task.notes.isNullOrBlank()) append(" • ${task.notes}")
        }

        val barColor = when {
            task.isOverdue && task.status != TaskStatus.DONE -> R.color.error
            task.priority == PriorityLevel.CAO -> R.color.error
            task.priority == PriorityLevel.TB -> R.color.amber
            else -> R.color.tertiary
        }
        val drawable = GradientDrawable().apply {
            cornerRadii = floatArrayOf(0f, 0f, 16f, 16f, 16f, 16f, 0f, 0f)
            setColor(ContextCompat.getColor(ctx, barColor))
        }
        b.priorityBar.background = drawable

        b.cbDone.setOnCheckedChangeListener(null)
        b.cbDone.isChecked = task.status == TaskStatus.DONE
        b.cbDone.setOnCheckedChangeListener { _, _ -> onToggleComplete(task.id) }

        b.contentArea.setOnClickListener { onSelect(task) }
        b.btnPlay.setOnClickListener {
            val next = if (task.status == TaskStatus.DOING) TaskStatus.DONE else TaskStatus.DOING
            onChangeStatus(task.id, next)
        }
        b.btnMore.setOnClickListener { v ->
            PopupMenu(ctx, v).apply {
                menu.add(0, 1, 0, "Xem chi tiết")
                menu.add(0, 2, 0, "Đang làm")
                menu.add(0, 3, 0, "Hoàn thành")
                menu.add(0, 4, 0, "Xóa nhiệm vụ")
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> onSelect(task)
                        2 -> onChangeStatus(task.id, TaskStatus.DOING)
                        3 -> onChangeStatus(task.id, TaskStatus.DONE)
                        4 -> onDelete(task.id)
                    }
                    true
                }
                show()
            }
        }
    }
}
