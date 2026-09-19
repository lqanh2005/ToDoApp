package com.lqanh.todoandroid.ui.adapters

import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lqanh.todoandroid.R
import com.lqanh.todoandroid.data.Task
import com.lqanh.todoandroid.data.TaskDateUtils
import com.lqanh.todoandroid.data.TaskStatus
import com.lqanh.todoandroid.databinding.ItemScheduleTaskBinding
import com.lqanh.todoandroid.databinding.ItemTaskBinding
import com.lqanh.todoandroid.ui.widgets.SwipeRevealLayout
import java.util.Calendar
import java.util.regex.Pattern

class TaskAdapter(
    private val onToggleComplete: (String) -> Unit,
    private val onChangeStatus: (String, TaskStatus) -> Unit,
    private val onDelete: (String) -> Unit,
    private val onAskCompletion: (Task) -> Unit = {},
    private val onEditSchedule: (Task) -> Unit = {},
    private val onOpenDetail: (Task) -> Unit = {},
    private val scheduleStyle: Boolean = false
) : ListAdapter<Task, RecyclerView.ViewHolder>(Diff) {

    object Diff : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
    }

    class FullVH(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)
    class ScheduleVH(val binding: ItemScheduleTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int = if (scheduleStyle) TYPE_SCHEDULE else TYPE_FULL

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_SCHEDULE) {
            ScheduleVH(ItemScheduleTaskBinding.inflate(inflater, parent, false))
        } else {
            FullVH(ItemTaskBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val task = getItem(position)
        when (holder) {
            is ScheduleVH -> bindSchedule(holder, task)
            is FullVH -> bindFull(holder, task)
        }
    }

    private fun bindSchedule(holder: ScheduleVH, task: Task) {
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

        val timeText = formatScheduleTime(task)
        bindMetaRow(
            timeView = b.tvTime,
            reminderView = b.ivReminder,
            subtaskIcon = b.ivSubtask,
            subtaskCount = b.tvSubtaskCount,
            metaRow = b.metaRow,
            task = task
        )

        val notDone = !done && (needsUpdate || task.isOverdue)
        val doing = task.status == TaskStatus.DOING && !notDone
        val (cardBg, cardStroke) = when {
            done -> R.color.task_bg_done to R.color.task_stroke_done
            doing -> R.color.task_bg_doing to R.color.task_stroke_doing
            notDone -> R.color.task_bg_not_done to R.color.task_stroke_not_done
            else -> R.color.white to R.color.slate_100
        }
        val density = ctx.resources.displayMetrics.density
        b.foregroundCard.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f * density
            setColor(ContextCompat.getColor(ctx, cardBg))
            setStroke((1f * density).toInt().coerceAtLeast(1), ContextCompat.getColor(ctx, cardStroke))
        }

        if (done) {
            b.btnCheckbox.setBackgroundResource(R.drawable.bg_checkbox_checked)
            b.ivCheck.visibility = View.VISIBLE
        } else {
            b.btnCheckbox.setBackgroundResource(R.drawable.bg_checkbox_unchecked)
            b.ivCheck.visibility = View.GONE
        }
        b.btnCheckbox.setOnClickListener { onToggleComplete(task.id) }
        b.foregroundCard.setOnClickListener { onOpenDetail(task) }

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
    }

    private fun bindFull(holder: FullVH, task: Task) {
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
        b.metaRow.alpha = if (done) 0.55f else 1f
        b.tvDescription.alpha = if (done) 0.55f else 1f
        b.btnCheckbox.alpha = if (done) 0.7f else 1f
        b.foregroundCard.alpha = 1f

        bindMetaRow(
            timeView = b.tvTime,
            reminderView = b.ivReminder,
            subtaskIcon = b.ivSubtask,
            subtaskCount = b.tvSubtaskCount,
            metaRow = b.metaRow,
            task = task
        )
        b.tvMeta.isVisible = false

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
            done -> "Done"
            needsUpdate -> "Update"
            task.status == TaskStatus.DOING -> "In progress"
            task.isOverdue -> "Not done"
            else -> "Not started"
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

        b.foregroundCard.setOnClickListener { onOpenDetail(task) }

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
                    menu.add(0, 1, 0, "Done")
                    menu.add(0, 2, 0, "Not done")
                }
                menu.add(0, 3, 0, "Delete task")
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

    private fun bindMetaRow(
        timeView: android.widget.TextView,
        reminderView: View,
        subtaskIcon: View,
        subtaskCount: android.widget.TextView,
        metaRow: View,
        task: Task
    ) {
        val timeText = formatScheduleTime(task)
        timeView.isVisible = timeText != null
        timeView.text = timeText.orEmpty()

        val hasReminder = hasReminder(task)
        reminderView.isVisible = hasReminder

        val totalSub = task.subtasks.size
        val doneSub = task.subtasks.count { it.completed }
        val hasSub = totalSub > 0
        subtaskIcon.isVisible = hasSub
        subtaskCount.isVisible = hasSub
        subtaskCount.text = "$doneSub/$totalSub"

        metaRow.isVisible = timeText != null || hasReminder || hasSub
    }

    private fun hasReminder(task: Task): Boolean {
        val rem = task.reminder.trim()
        return rem.isNotEmpty() && rem != "0"
    }

    private fun formatScheduleTime(task: Task): String? {
        task.startAtMillis?.let { millis ->
            val cal = Calendar.getInstance().apply { timeInMillis = millis }
            return "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        }
        val raw = task.dueTime.ifBlank { task.dueDate }
        val parsed = TaskDateUtils.parseTimeOfDay(raw)
        if (parsed != null) {
            return "%02d:%02d".format(parsed.first, parsed.second)
        }
        val matcher = TIME_PATTERN.matcher(raw)
        if (matcher.find()) {
            val hour = matcher.group(1)?.toIntOrNull()
            val minute = matcher.group(2)?.toIntOrNull()
            if (hour != null && minute != null) return "%02d:%02d".format(hour, minute)
            return matcher.group()
        }
        return null
    }

    companion object {
        private const val TYPE_FULL = 0
        private const val TYPE_SCHEDULE = 1
        private val TIME_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{2})")

        fun closeOpenSwipe() = SwipeRevealLayout.closeOpen()
    }
}
