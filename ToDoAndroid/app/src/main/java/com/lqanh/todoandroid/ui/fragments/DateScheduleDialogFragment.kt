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
import com.lqanh.todoandroid.databinding.DialogDateScheduleBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogDateScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setGravity(Gravity.CENTER)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.getLong(ARG_INITIAL, -1L)?.takeIf { it > 0 }?.let {
            selectedDay = startOfDay(Calendar.getInstance().apply { timeInMillis = it })
            displayMonth.timeInMillis = it
            displayMonth.set(Calendar.DAY_OF_MONTH, 1)
            startOfDay(displayMonth)
            syncQuickFromSelected()
        }

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

    private fun renderCalendar() {
        binding.tvMonthYear.text = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(displayMonth.time)
        binding.gridDays.removeAllViews()

        val first = startOfDay(displayMonth.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val startOffset = first.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
        val cursor = (first.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -startOffset) }
        val cellSize = ((resources.displayMetrics.widthPixels * 0.92f) - (32 * resources.displayMetrics.density)) / 7f

        repeat(42) {
            val dayCal = cursor.clone() as Calendar
            val cell = layoutInflater.inflate(R.layout.item_calendar_day, binding.gridDays, false) as TextView
            cell.layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = cellSize.toInt()
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
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

    private fun applyDefaultReminder() {
        reminderKey = ReminderDialogFragment.OPTION_5_MIN
        reminder = "5 min"
        reminderCustom = null
        reminderManual = false
    }

    private fun clearReminder() {
        reminder = null
        reminderKey = null
        reminderCustom = null
    }

    private fun reminderOffsetMinutes(): Int? = when (reminderKey) {
        ReminderDialogFragment.OPTION_AT_TASK -> 0
        ReminderDialogFragment.OPTION_5_MIN -> 5
        ReminderDialogFragment.OPTION_15_MIN -> 15
        ReminderDialogFragment.OPTION_30_MIN -> 30
        else -> null
    }

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
            return "%02d:%02d".format(
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE)
            )
        }
        return when (key) {
            ReminderDialogFragment.OPTION_CUSTOM -> reminderCustom ?: reminder
            else -> reminder
        }
    }

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

    private fun updateOptionLabels() {
        if (timeHour != null && timeMinute != null) {
            binding.tvTimeValue.text = "%02d:%02d".format(timeHour, timeMinute)
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

    private fun isSameDay(a: Calendar, b: Calendar): Boolean {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }

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

        fun newInstance(initialMillis: Long): DateScheduleDialogFragment {
            return DateScheduleDialogFragment().apply {
                arguments = bundleOf(ARG_INITIAL to initialMillis)
            }
        }

        private fun startOfDay(cal: Calendar): Calendar = cal.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        private fun startOfMonth(cal: Calendar): Calendar = startOfDay(cal).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
}
