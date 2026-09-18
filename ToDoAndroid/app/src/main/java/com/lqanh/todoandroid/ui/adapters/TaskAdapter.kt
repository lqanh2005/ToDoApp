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
import com.lqanh.todoandroid.data.Task
import com.lqanh.todoandroid.data.TaskDateUtils
import com.lqanh.todoandroid.data.TaskStatus
import com.lqanh.todoandroid.databinding.ItemTaskBinding
import com.lqanh.todoandroid.ui.widgets.SwipeRevealLayout

class TaskAdapter(
    private val onToggleComplete: (String) -> Unit,
    private val onChangeStatus: (String, TaskStatus) -> Unit,
    private val onDelete: (String) -> Unit,
    private val onAskCompletion: (Task) -> Unit = {},
    private val onEditSchedule: (Task) -> Unit = {}
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
        val now = System.currentTimeMillis()
        val endAt = TaskDateUtils.resolveEndAtMillis(task)
        val needsUpdate = task.awaitingCompletion || (endAt != null && now >= endAt && !done)

        b.swipeLayout.close(animate = false)

        b.tvTaskTitle.text = task.title
        b.tvTaskTitle.paintFlags = if (done) {
            b.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            b.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        b.tvTaskTitle.setTextColor(
            ContextCompat.getColor(ctx, if (done) R.color.slate_400 else R.color.on_surface)
        )
        b.tvTaskTitle.alpha = if (done) 0.55f else 1f
        b.tvMeta.alpha = if (done) 0.55f else 1f
        b.tvDescription.alpha = if (done) 0.55f else 1f
        b.btnCheckbox.alpha = if (done) 0.7f else 1f
        b.foregroundCard.alpha = 1f

        b.tvMeta.text = formatDueLabel(task)
        val desc = task.description?.trim().orEmpty()
        b.tvDescription.text = desc
        b.tvDescription.visibility = if (desc.isBlank()) View.GONE else View.VISIBLE

        val notDone = !done && (needsUpdate || task.isOverdue)
        val doing = task.status == TaskStatus.DOING && !notDone

        val (cardBg, cardStroke, barColor) = when {
            done -> Triple(R.color.task_bg_done, R.color.task_stroke_done, R.color.status_done)
            doing -> Triple(R.color.task_bg_doing, R.color.task_stroke_doing, R.color.status_doing)
            notDone -> Triple(R.color.task_bg_not_done, R.color.task_stroke_not_done, R.color.status_not_done)
            else -> Triple(R.color.white, R.color.slate_100, R.color.tertiary)
        }
        val density = ctx.resources.displayMetrics.density
        b.foregroundCard.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f * density
            setColor(ContextCompat.getColor(ctx, cardBg))
            setStroke((1f * density).toInt().coerceAtLeast(1), ContextCompat.getColor(ctx, cardStroke))
        }
        b.priorityBar.background = GradientDrawable().apply {
            cornerRadii = floatArrayOf(12f * density, 12f * density, 0f, 0f, 0f, 0f, 12f * density, 12f * density)
            setColor(ContextCompat.getColor(ctx, barColor))
        }

        if (done) {
            b.btnCheckbox.setBackgroundResource(R.drawable.bg_checkbox_checked)
            b.ivCheck.visibility = View.VISIBLE
        } else {
            b.btnCheckbox.setBackgroundResource(R.drawable.bg_checkbox_unchecked)
            b.ivCheck.visibility = View.GONE
        }
        b.btnCheckbox.setOnClickListener { onToggleComplete(task.id) }

        val label = when {
            done -> "Xong"
            needsUpdate -> "Cập nhật"
            task.status == TaskStatus.DOING -> "Đang làm"
            task.isOverdue -> "Chưa xong"
            else -> "Chưa tới giờ"
        }
        b.tvStart.text = label

        when {
            done -> {
                b.btnStart.setBackgroundResource(R.drawable.bg_status_green)
                applyStatusTint(b, R.color.status_done)
            }
            doing -> {
                b.btnStart.setBackgroundResource(R.drawable.bg_status_green)
                applyStatusTint(b, R.color.status_doing)
            }
            else -> {
                b.btnStart.setBackgroundResource(R.drawable.bg_status_amber)
                applyStatusTint(b, R.color.status_not_done)
            }
        }

        b.btnStart.setOnClickListener {
            if (needsUpdate) onAskCompletion(task)
        }

        b.btnSwipeSchedule.setOnClickListener {
            b.swipeLayout.close()
            onEditSchedule(task)
        }
        b.btnSwipeDone.setOnClickListener {
            b.swipeLayout.close()
            onChangeStatus(task.id, TaskStatus.DONE)
        }
        b.btnSwipeNotDone.setOnClickListener {
            b.swipeLayout.close()
            onChangeStatus(task.id, TaskStatus.TODO)
        }
        b.btnSwipeDelete.setOnClickListener {
            b.swipeLayout.close()
            onDelete(task.id)
        }

        b.btnMore.setOnClickListener { v ->
            PopupMenu(ctx, v).apply {
                if (needsUpdate) {
                    menu.add(0, 1, 0, "Đã xong")
                    menu.add(0, 2, 0, "Chưa xong")
                }
                menu.add(0, 3, 0, "Xóa nhiệm vụ")
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> onChangeStatus(task.id, TaskStatus.DONE)
                        2 -> onChangeStatus(task.id, TaskStatus.TODO)
                        3 -> onDelete(task.id)
                    }
                    true
                }
                show()
            }
        }
    }

    private fun applyStatusTint(b: ItemTaskBinding, colorRes: Int) {
        val color = ContextCompat.getColor(b.root.context, colorRes)
        b.tvStart.setTextColor(color)
        b.btnStart.getChildAt(0)?.let { icon ->
            if (icon is android.widget.ImageView) {
                icon.setColorFilter(color)
            }
        }
    }

    private fun formatDueLabel(task: Task): String {
        val raw = task.dueTime.ifBlank { task.dueDate }.trim()
        return raw
            .replace("Hôm nay", "hôm nay")
            .replace("Ngày mai", "Ngày mai")
    }

    companion object {
        fun closeOpenSwipe() = SwipeRevealLayout.closeOpen()
    }
}
