package com.lqanh.todoandroid.ui.adapters

import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.lqanh.todoandroid.databinding.ItemTaskSectionHeaderBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

enum class TaskSection {
    PREVIOUS,
    TODAY,
    FUTURE,
    COMPLETE
}

sealed class TaskListItem {
    data class Header(
        val section: TaskSection,
        val count: Int,
        val expanded: Boolean
    ) : TaskListItem()

    data class Row(
        val task: Task,
        val section: TaskSection
    ) : TaskListItem()
}

class GroupedTaskAdapter(
    private val onToggleComplete: (String) -> Unit,
    private val onChangeStatus: (String, TaskStatus) -> Unit,
    private val onDelete: (String) -> Unit,
    private val onEditSchedule: (Task) -> Unit = {},
    private val onOpenDetail: (Task) -> Unit = {},
    private val onToggleSection: (TaskSection) -> Unit
) : ListAdapter<TaskListItem, RecyclerView.ViewHolder>(Diff) {

    object Diff : DiffUtil.ItemCallback<TaskListItem>() {
        override fun areItemsTheSame(oldItem: TaskListItem, newItem: TaskListItem): Boolean {
            return when {
                oldItem is TaskListItem.Header && newItem is TaskListItem.Header ->
                    oldItem.section == newItem.section
                oldItem is TaskListItem.Row && newItem is TaskListItem.Row ->
                    oldItem.task.id == newItem.task.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: TaskListItem, newItem: TaskListItem): Boolean =
            oldItem == newItem
    }

    class HeaderVH(val binding: ItemTaskSectionHeaderBinding) : RecyclerView.ViewHolder(binding.root)
    class TaskVH(val binding: ItemScheduleTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is TaskListItem.Header -> TYPE_HEADER
        is TaskListItem.Row -> TYPE_TASK
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderVH(ItemTaskSectionHeaderBinding.inflate(inflater, parent, false))
        } else {
            TaskVH(ItemScheduleTaskBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TaskListItem.Header -> bindHeader(holder as HeaderVH, item)
            is TaskListItem.Row -> bindTask(holder as TaskVH, item)
        }
    }

    private fun bindHeader(holder: HeaderVH, item: TaskListItem.Header) {
        val title = when (item.section) {
            TaskSection.PREVIOUS -> "Previous"
            TaskSection.TODAY -> "Today"
            TaskSection.FUTURE -> "Future"
            TaskSection.COMPLETE -> "Complete"
        }
        holder.binding.tvSectionTitle.text = "$title (${item.count})"
        holder.binding.ivSectionChevron.rotation = if (item.expanded) 0f else 180f
        holder.binding.root.setOnClickListener { onToggleSection(item.section) }
    }

    private fun bindTask(holder: TaskVH, item: TaskListItem.Row) {
        val task = item.task
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

        val meta = formatSectionMeta(task, item.section)
        b.tvTime.isVisible = meta != null
        b.tvTime.text = meta.orEmpty()
        val metaColor = when {
            item.section == TaskSection.PREVIOUS && !done -> ContextCompat.getColor(ctx, R.color.error)
            else -> ContextCompat.getColor(ctx, R.color.slate_500)
        }
        b.tvTime.setTextColor(metaColor)

        val hasReminder = task.reminder.isNotBlank() && task.reminder != "0"
        b.ivReminder.isVisible = hasReminder

        val totalSub = task.subtasks.size
        val doneSub = task.subtasks.count { it.completed }
        val hasSub = totalSub > 0
        b.ivSubtask.isVisible = hasSub
        b.tvSubtaskCount.isVisible = hasSub
        b.tvSubtaskCount.text = "$doneSub/$totalSub"

        b.metaRow.isVisible = meta != null || hasReminder || hasSub

        val notDone = !done && (needsUpdate || task.isOverdue)
        val doing = task.status == TaskStatus.DOING && !notDone
        val (cardBg, cardStroke) = when {
            done -> R.color.task_bg_done to R.color.task_stroke_done
            doing -> R.color.task_bg_doing to R.color.task_stroke_doing
            notDone || item.section == TaskSection.PREVIOUS ->
                R.color.task_bg_not_done to R.color.task_stroke_not_done
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

    private fun formatSectionMeta(task: Task, section: TaskSection): String? {
        val dayMillis = TaskDateUtils.resolveDueDayMillis(task)
        val time = extractTime(task)
        return when (section) {
            TaskSection.TODAY -> time
            TaskSection.PREVIOUS, TaskSection.FUTURE, TaskSection.COMPLETE -> {
                val datePart = dayMillis?.let {
                    SimpleDateFormat("MM-dd", Locale.US).format(Date(it))
                }
                when {
                    datePart != null && time != null -> "$datePart $time"
                    datePart != null -> datePart
                    time != null -> time
                    else -> null
                }
            }
        }
    }

    private fun extractTime(task: Task): String? {
        task.startAtMillis?.let { millis ->
            val cal = Calendar.getInstance().apply { timeInMillis = millis }
            return "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        }
        val raw = task.dueTime.ifBlank { task.dueDate }
        val parsed = TaskDateUtils.parseTimeOfDay(raw)
        if (parsed != null) return "%02d:%02d".format(parsed.first, parsed.second)
        val matcher = TIME_PATTERN.matcher(raw)
        if (matcher.find()) {
            val h = matcher.group(1)?.toIntOrNull() ?: return null
            val m = matcher.group(2)?.toIntOrNull() ?: return null
            return "%02d:%02d".format(h, m)
        }
        return null
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_TASK = 1
        private val TIME_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{2})")

        fun buildItems(
            tasks: List<Task>,
            expanded: Map<TaskSection, Boolean>
        ): List<TaskListItem> {
            val today = TaskDateUtils.startOfDay()
            val incomplete = tasks.filter { it.status != TaskStatus.DONE }
            val complete = tasks.filter { it.status == TaskStatus.DONE }

            val previous = incomplete.filter { task ->
                val day = TaskDateUtils.resolveDueDayMillis(task) ?: return@filter false
                day < today
            }
            val todayTasks = incomplete.filter { task ->
                val day = TaskDateUtils.resolveDueDayMillis(task) ?: return@filter false
                TaskDateUtils.isSameDay(day, today)
            }
            val future = incomplete.filter { task ->
                val day = TaskDateUtils.resolveDueDayMillis(task)
                day == null || day > today
            }

            val sections = listOf(
                TaskSection.PREVIOUS to previous,
                TaskSection.TODAY to todayTasks,
                TaskSection.FUTURE to future,
                TaskSection.COMPLETE to complete
            )

            val items = mutableListOf<TaskListItem>()
            sections.forEach { (section, sectionTasks) ->
                if (sectionTasks.isEmpty()) return@forEach
                val isExpanded = expanded[section] != false
                items += TaskListItem.Header(section, sectionTasks.size, isExpanded)
                if (isExpanded) {
                    sectionTasks.forEach { items += TaskListItem.Row(it, section) }
                }
            }
            return items
        }
    }
}
