package com.lqanh.todoandroid.ui.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.lqanh.todoandroid.R
import com.lqanh.todoandroid.data.Task
import com.lqanh.todoandroid.data.TaskDateUtils
import com.lqanh.todoandroid.ui.DialogSizing
import com.lqanh.todoandroid.databinding.DialogDateScheduleBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Hộp thoại chọn ngày (chọn nhanh hoặc từ lịch), giờ, nhắc nhở và lặp lại; trả kết quả qua Fragment Result.
class DateScheduleDialogFragment : DialogFragment() {

    private var _binding: DialogDateScheduleBinding? = null
    private val binding get() = _binding!!

    private val displayMonth: Calendar = startOfMonth(Calendar.getInstance())
    private var selectedDay: Calendar? = startOfDay(Calendar.getInstance())
    private var noDate = false
    private var timeHour: Int? = null
    private var timeMinute: Int? = null
    private var reminder: String? = null
    private var reminderKey: String? = null
    private var reminderCustom: String? = null
    private var reminderManual = false
    private var repeat: String? = null
    private var quickKey: String? = "today"

    // Tạo dialog với nền trong suốt để thấy bo góc của layout.
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    // Tạo view từ layout dialog_date_schedule.xml.
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogDateScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Đặt kích thước dialog rộng 92% màn hình và căn giữa.
    override fun onStart() {
        super.onStart()
        DialogSizing.apply(dialog)
    }

    // Khởi tạo ngày ban đầu, gắn sự kiện và nhận kết quả từ hộp thoại chọn giờ/nhắc nhở.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.getLong(ARG_INITIAL, -1L)?.takeIf { it > 0 }?.let {
            selectedDay = startOfDay(Calendar.getInstance().apply { timeInMillis = it })
            displayMonth.timeInMillis = it
            displayMonth.set(Calendar.DAY_OF_MONTH, 1)
            startOfDay(displayMonth)
            syncQuickFromSelected()
        }
        restoreTaskTime()

        binding.btnPrevMonth.setOnClickListener {
            displayMonth.add(Calendar.MONTH, -1)
            renderCalendar()
        }
        binding.btnNextMonth.setOnClickListener {
            displayMonth.add(Calendar.MONTH, 1)
            renderCalendar()
        }

        binding.chipToday.setOnClickListener { selectQuick("today") }
        binding.chipTomorrow.setOnClickListener { selectQuick("tomorrow") }
        binding.chipThreeDays.setOnClickListener { selectQuick("three") }
        binding.chipThisSunday.setOnClickListener { selectQuick("sunday") }
        binding.chipNoDate.setOnClickListener { selectQuick("none") }

        binding.rowTime.setOnClickListener { pickTime() }
        binding.rowReminder.setOnClickListener { pickReminder() }
        binding.rowRepeat.setOnClickListener { pickRepeat() }

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnDone.setOnClickListener { finishDone() }

        childFragmentManager.setFragmentResultListener(
            SetTimeDialogFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            if (bundle.getBoolean(SetTimeDialogFragment.KEY_NO_TIME)) {
                timeHour = null
                timeMinute = null
                if (!reminderManual) {
                    clearReminder()
                }
            } else {
                timeHour = bundle.getInt(SetTimeDialogFragment.KEY_HOUR)
                timeMinute = bundle.getInt(SetTimeDialogFragment.KEY_MINUTE)
                if (!reminderManual || reminderKey == null) {
                    applyDefaultReminder()
                }
            }
            updateOptionLabels()
        }

        childFragmentManager.setFragmentResultListener(
            ReminderDialogFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            reminderManual = true
            val enabled = bundle.getBoolean(ReminderDialogFragment.KEY_ENABLED)
            if (!enabled) {
                clearReminder()
            } else {
                reminderKey = bundle.getString(ReminderDialogFragment.KEY_VALUE)
                reminder = bundle.getString(ReminderDialogFragment.KEY_LABEL)
                reminderCustom = bundle.getString(ReminderDialogFragment.KEY_CUSTOM_LABEL)
            }
            updateOptionLabels()
        }

        renderCalendar()
        updateQuickChips()
        updateOptionLabels()
    }

    // Nạp giờ và nhắc nhở hiện có của task (nếu mở từ forTask) để chỉ đổi ngày thì không mất giờ.
    private fun restoreTaskTime() {
        val args = arguments ?: return
        val h = args.getInt(ARG_HOUR, -1)
        val m = args.getInt(ARG_MINUTE, -1)
        if (h !in 0..23 || m !in 0..59) return
        timeHour = h
        timeMinute = m
        val (key, label) = when (args.getString(ARG_REMINDER).orEmpty()) {
            "5" -> ReminderDialogFragment.OPTION_5_MIN to "5 min"
            "15" -> ReminderDialogFragment.OPTION_15_MIN to "15 min"
            "30" -> ReminderDialogFragment.OPTION_30_MIN to "30 min"
            "1440" -> ReminderDialogFragment.OPTION_1_DAY to "1 day"
            else -> null to null
        }
        reminderKey = key
        reminder = label
        reminderManual = true
    }

    // Gửi toàn bộ lựa chọn về màn hình gọi rồi đóng dialog.
    private fun finishDone() {
        setFragmentResult(
            REQUEST_KEY,
            bundleOf(
                KEY_NO_DATE to noDate,
                KEY_MILLIS to (selectedDay?.timeInMillis ?: -1L),
                KEY_HAS_TIME to (timeHour != null),
                KEY_HOUR to (timeHour ?: -1),
                KEY_MINUTE to (timeMinute ?: -1),
                KEY_REMINDER to reminderResultLabel(),
                KEY_REPEAT to (repeat ?: "")
            )
        )
        dismiss()
    }

    // Chọn nhanh: hôm nay, ngày mai, 3 ngày nữa, Chủ nhật này hoặc không có ngày.
    private fun selectQuick(key: String) {
        quickKey = key
        noDate = key == "none"
        if (noDate) {
            selectedDay = null
        } else {
            val cal = startOfDay(Calendar.getInstance())
            when (key) {
                "tomorrow" -> cal.add(Calendar.DAY_OF_YEAR, 1)
                "three" -> cal.add(Calendar.DAY_OF_YEAR, 3)
                "sunday" -> {
                    val dow = cal.get(Calendar.DAY_OF_WEEK)
                    var add = Calendar.SUNDAY - dow
                    if (add <= 0) add += 7
                    cal.add(Calendar.DAY_OF_YEAR, add)
                }
            }
            selectedDay = cal
            displayMonth.timeInMillis = cal.timeInMillis
            displayMonth.set(Calendar.DAY_OF_MONTH, 1)
            startOfDay(displayMonth)
        }
        updateQuickChips()
        renderCalendar()
    }

    // Từ ngày đang chọn, xác định chip chọn nhanh tương ứng (nếu có).
    private fun syncQuickFromSelected() {
        val sel = selectedDay ?: run {
            quickKey = "none"
            noDate = true
            return
        }
        noDate = false
        val today = startOfDay(Calendar.getInstance())
        val tomorrow = startOfDay(Calendar.getInstance()).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val three = startOfDay(Calendar.getInstance()).apply { add(Calendar.DAY_OF_YEAR, 3) }
        val sunday = startOfDay(Calendar.getInstance()).apply {
            var add = Calendar.SUNDAY - get(Calendar.DAY_OF_WEEK)
            if (add <= 0) add += 7
            add(Calendar.DAY_OF_YEAR, add)
        }
        quickKey = when (sel.timeInMillis) {
            today.timeInMillis -> "today"
            tomorrow.timeInMillis -> "tomorrow"
            three.timeInMillis -> "three"
            sunday.timeInMillis -> "sunday"
            else -> null
        }
    }

    // Tô sáng chip chọn nhanh đang được chọn.
    private fun updateQuickChips() {
        listOf(
            binding.chipToday to "today",
            binding.chipTomorrow to "tomorrow",
            binding.chipThreeDays to "three",
            binding.chipThisSunday to "sunday",
            binding.chipNoDate to "none"
        ).forEach { (view, key) ->
            view.isSelected = quickKey == key
        }
    }

    // Vẽ lưới 42 ô ngày của tháng đang xem; bấm vào ô để chọn ngày.
    private fun renderCalendar() {
        binding.tvMonthYear.text = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(displayMonth.time)
        binding.gridDays.removeAllViews()

        val first = startOfDay(displayMonth.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val startOffset = first.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
        val cursor = (first.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -startOffset) }
        val cellSize = calendarCellSize()

        repeat(42) {
            val dayCal = cursor.clone() as Calendar
            val cell = layoutInflater.inflate(R.layout.item_calendar_day, binding.gridDays, false) as TextView
            cell.layoutParams = GridLayout.LayoutParams().apply {
                width = cellSize
                height = cellSize
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.CENTER, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED)
            }
            cell.text = dayCal.get(Calendar.DAY_OF_MONTH).toString()

            val inMonth = dayCal.get(Calendar.MONTH) == displayMonth.get(Calendar.MONTH)
            val isSelected = !noDate && selectedDay != null && isSameDay(dayCal, selectedDay!!)

            when {
                isSelected -> {
                    cell.setBackgroundResource(R.drawable.bg_day_selected)
                    cell.setTextColor(Color.WHITE)
                }
                inMonth -> {
                    cell.background = null
                    cell.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_700))
                }
                else -> {
                    cell.background = null
                    cell.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_400))
                }
            }

            cell.setOnClickListener {
                noDate = false
                selectedDay = startOfDay(dayCal.clone() as Calendar)
                if (!inMonth) {
                    displayMonth.timeInMillis = dayCal.timeInMillis
                    displayMonth.set(Calendar.DAY_OF_MONTH, 1)
                    startOfDay(displayMonth)
                }
                syncQuickFromSelected()
                updateQuickChips()
                renderCalendar()
            }
            binding.gridDays.addView(cell)
            cursor.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    // Cạnh của mỗi ô ngày: vừa 7 cột theo chiều rộng hộp thoại, không quá 6% chiều cao màn hình và không quá 44dp.
    private fun calendarCellSize(): Int {
        val metrics = resources.displayMetrics
        val byWidth = (DialogSizing.dialogWidthPx(requireContext()) - 32 * metrics.density) / 7f
        val byHeight = metrics.heightPixels * 0.06f
        return minOf(byWidth, byHeight, 44 * metrics.density).toInt()
    }

    // Mở hộp thoại chọn giờ.
    private fun pickTime() {
        if (childFragmentManager.findFragmentByTag(SetTimeDialogFragment.TAG) != null) return
        SetTimeDialogFragment
            .newInstance(
                hasTime = timeHour != null,
                hour = timeHour ?: 17,
                minute = timeMinute ?: 0
            )
            .show(childFragmentManager, SetTimeDialogFragment.TAG)
    }

    // Mở hộp thoại chọn nhắc nhở.
    private fun pickReminder() {
        if (childFragmentManager.findFragmentByTag(ReminderDialogFragment.TAG) != null) return
        ReminderDialogFragment
            .newInstance(
                enabled = reminder != null,
                key = reminderKey,
                custom = reminderCustom
            )
            .show(childFragmentManager, ReminderDialogFragment.TAG)
    }

    // Hiện popup chọn kiểu lặp lại (không, hằng ngày, hằng tuần, hằng tháng).
    private fun pickRepeat() {
        val options = listOf(
            null to "No",
            "Daily" to "Daily",
            "Weekly" to "Weekly",
            "Monthly" to "Monthly"
        )

        val content = layoutInflater.inflate(R.layout.popup_repeat_menu, null)
        val list = content as LinearLayout

        val popup = PopupWindow(
            content,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            elevation = 10f
            isOutsideTouchable = true
        }

        options.forEach { (value, label) ->
            val row = layoutInflater.inflate(R.layout.item_category_option, list, false) as TextView
            row.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (34 * resources.displayMetrics.density).toInt()
            )
            row.text = label
            val selected = repeat == value
            if (selected) {
                row.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                row.setTypeface(row.typeface, Typeface.BOLD)
            }
            row.setOnClickListener {
                repeat = value
                updateOptionLabels()
                popup.dismiss()
            }
            list.addView(row)
        }

        content.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val anchor = binding.tvRepeatValue
        val xOff = anchor.width - content.measuredWidth
        popup.showAsDropDown(anchor, xOff, (4 * resources.displayMetrics.density).toInt(), Gravity.START)
    }

    // Đặt nhắc nhở mặc định 5 phút trước khi vừa chọn giờ.
    private fun applyDefaultReminder() {
        reminderKey = ReminderDialogFragment.OPTION_5_MIN
        reminder = "5 min"
        reminderCustom = null
        reminderManual = false
    }

    // Xóa nhắc nhở.
    private fun clearReminder() {
        reminder = null
        reminderKey = null
        reminderCustom = null
    }

    // Số phút nhắc trước tương ứng với lựa chọn hiện tại.
    private fun reminderOffsetMinutes(): Int? = when (reminderKey) {
        ReminderDialogFragment.OPTION_AT_TASK -> 0
        ReminderDialogFragment.OPTION_5_MIN -> 5
        ReminderDialogFragment.OPTION_15_MIN -> 15
        ReminderDialogFragment.OPTION_30_MIN -> 30
        else -> null
    }

    // Chuỗi hiển thị nhắc nhở: giờ nhắc cụ thể nếu tính được, ngược lại là nhãn lựa chọn.
    private fun formatReminderDisplay(): String? {
        val key = reminderKey ?: return null
        val h = timeHour
        val m = timeMinute
        val offset = reminderOffsetMinutes()
        if (h != null && m != null && offset != null) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MINUTE, -offset)
            }
            return TaskDateUtils.formatClock(
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE)
            )
        }
        return when (key) {
            ReminderDialogFragment.OPTION_CUSTOM -> reminderCustom ?: reminder
            else -> reminder
        }
    }

    // Nhãn nhắc nhở gửi về màn hình gọi ("5 min", "1 day"...).
    private fun reminderResultLabel(): String {
        return when (reminderKey) {
            null -> ""
            ReminderDialogFragment.OPTION_AT_TASK -> "At task time"
            ReminderDialogFragment.OPTION_5_MIN -> "5 min"
            ReminderDialogFragment.OPTION_15_MIN -> "15 min"
            ReminderDialogFragment.OPTION_30_MIN -> "30 min"
            ReminderDialogFragment.OPTION_1_DAY -> "1 day"
            ReminderDialogFragment.OPTION_CUSTOM -> reminderCustom ?: reminder ?: ""
            else -> reminder ?: ""
        }
    }

    // Cập nhật chữ và màu của các dòng Giờ, Nhắc nhở, Lặp lại.
    private fun updateOptionLabels() {
        val h = timeHour
        val m = timeMinute
        if (h != null && m != null) {
            binding.tvTimeValue.text = TaskDateUtils.formatClock(h, m)
            binding.tvTimeValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_700))
        } else {
            binding.tvTimeValue.text = "No"
            binding.tvTimeValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.slate_400))
        }

        val remDisplay = formatReminderDisplay()
        binding.tvReminderValue.text = remDisplay ?: "No"
        val remActive = remDisplay != null
        binding.tvReminderValue.setTextColor(
            ContextCompat.getColor(requireContext(), if (remActive) R.color.slate_700 else R.color.slate_400)
        )
        binding.tvReminderLabel.setTextColor(
            ContextCompat.getColor(requireContext(), if (remActive) R.color.slate_700 else R.color.slate_400)
        )

        val rep = repeat
        binding.tvRepeatValue.text = rep ?: "No"
        binding.tvRepeatValue.setTextColor(
            ContextCompat.getColor(requireContext(), if (rep != null) R.color.slate_700 else R.color.slate_400)
        )
    }

    // Kiểm tra hai Calendar có cùng ngày không.
    private fun isSameDay(a: Calendar, b: Calendar): Boolean {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }

    // Giải phóng binding khi view bị hủy.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DateScheduleDialog"
        const val REQUEST_KEY = "date_schedule_result"
        const val KEY_NO_DATE = "no_date"
        const val KEY_MILLIS = "millis"
        const val KEY_HAS_TIME = "has_time"
        const val KEY_HOUR = "hour"
        const val KEY_MINUTE = "minute"
        const val KEY_REMINDER = "reminder"
        const val KEY_REPEAT = "repeat"
        private const val ARG_INITIAL = "initial"
        private const val ARG_HOUR = "arg_hour"
        private const val ARG_MINUTE = "arg_minute"
        private const val ARG_REMINDER = "arg_reminder"

        // Tạo dialog với ngày ban đầu được chọn sẵn.
        fun newInstance(initialMillis: Long): DateScheduleDialogFragment {
            return DateScheduleDialogFragment().apply {
                arguments = bundleOf(ARG_INITIAL to initialMillis)
            }
        }

        // Tạo dialog sửa lịch của task: chọn sẵn ngày, giờ và nhắc nhở hiện tại của task.
        fun forTask(task: Task): DateScheduleDialogFragment {
            val initial = task.dueAtMillis
                ?: TaskDateUtils.resolveDueDayMillis(task)
                ?: System.currentTimeMillis()
            val start = TaskDateUtils.resolveStartAtMillis(task)?.let {
                Calendar.getInstance().apply { timeInMillis = it }
            }
            return DateScheduleDialogFragment().apply {
                arguments = bundleOf(
                    ARG_INITIAL to initial,
                    ARG_HOUR to (start?.get(Calendar.HOUR_OF_DAY) ?: -1),
                    ARG_MINUTE to (start?.get(Calendar.MINUTE) ?: -1),
                    ARG_REMINDER to task.reminder
                )
            }
        }

        // Đưa Calendar về 00:00:00 của ngày đó.
        private fun startOfDay(cal: Calendar): Calendar = cal.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Đưa Calendar về ngày 1 của tháng lúc 00:00:00.
        private fun startOfMonth(cal: Calendar): Calendar = startOfDay(cal).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
}
