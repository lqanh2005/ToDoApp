package com.lqanh.todoandroid.ui.adapters

import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
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
        val done = task.status == TaskStatus.DONE

        b.tvTaskTitle.text = task.title
        b.tvTaskTitle.paintFlags = if (done) {
            b.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            b.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        b.tvTaskTitle.setTextColor(
            ContextCompat.getColor(ctx, if (done) R.color.slate_400 else R.color.on_surface)
        )
        b.root.alpha = if (done) 0.6f else 1f

        b.tvMeta.text = formatDueLabel(task)
        val desc = task.description?.trim().orEmpty()
        b.tvDescription.text = desc
        b.tvDescription.visibility = if (desc.isBlank()) View.GONE else View.VISIBLE

        val barColor = when {
            task.isOverdue && !done -> R.color.error
            task.priority == PriorityLevel.CAO -> R.color.error
            task.priority == PriorityLevel.TB -> R.color.amber
            else -> R.color.tertiary
        }
        val drawable = GradientDrawable().apply {
            cornerRadii = floatArrayOf(12f, 12f, 0f, 0f, 0f, 0f, 12f, 12f)
            setColor(ContextCompat.getColor(ctx, barColor))
        }
        b.priorityBar.background = drawable

        if (done) {
            b.btnCheckbox.setBackgroundResource(R.drawable.bg_checkbox_checked)
            b.ivCheck.visibility = View.VISIBLE
        } else {
            b.btnCheckbox.setBackgroundResource(R.drawable.bg_checkbox_unchecked)
            b.ivCheck.visibility = View.GONE
        }
        b.btnCheckbox.setOnClickListener { onToggleComplete(task.id) }

        val highlightStart = position == 0 && !done
        if (highlightStart) {
            b.btnStart.setBackgroundResource(R.drawable.bg_start_btn)
            b.tvStart.setTextColor(ContextCompat.getColor(ctx, R.color.primary))
            b.btnStart.getChildAt(0)?.let { icon ->
                if (icon is android.widget.ImageView) {
                    icon.setColorFilter(ContextCompat.getColor(ctx, R.color.primary))
                }
            }
        } else {
            b.btnStart.setBackgroundResource(R.drawable.bg_start_btn_muted)
            b.tvStart.setTextColor(ContextCompat.getColor(ctx, R.color.slate_700))
            b.btnStart.getChildAt(0)?.let { icon ->
                if (icon is android.widget.ImageView) {
                    icon.setColorFilter(ContextCompat.getColor(ctx, R.color.slate_700))
                }
            }
        }

        b.tvStart.text = when (task.status) {
            TaskStatus.DOING -> "Đang làm"
            TaskStatus.DONE -> "Xong"
            else -> ctx.getString(R.string.start_task)
        }

        b.btnStart.setOnClickListener {
            val next = when (task.status) {
                TaskStatus.TODO -> TaskStatus.DOING
                TaskStatus.DOING -> TaskStatus.DONE
                TaskStatus.DONE -> TaskStatus.TODO
            }
            onChangeStatus(task.id, next)
        }

        b.btnMore.setOnClickListener { v ->
            PopupMenu(ctx, v).apply {
                menu.add(0, 1, 0, "Đang làm")
                menu.add(0, 2, 0, "Hoàn thành")
                menu.add(0, 3, 0, "Xóa nhiệm vụ")
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> onChangeStatus(task.id, TaskStatus.DOING)
                        2 -> onChangeStatus(task.id, TaskStatus.DONE)
                        3 -> onDelete(task.id)
                    }
                    true
                }
                show()
            }
        }
    }

    private fun formatDueLabel(task: Task): String {
        val raw = task.dueTime.ifBlank { task.dueDate }.trim()
        return raw
            .replace("Hôm nay", "hôm nay")
            .replace("Ngày mai", "Ngày mai")
    }
}
