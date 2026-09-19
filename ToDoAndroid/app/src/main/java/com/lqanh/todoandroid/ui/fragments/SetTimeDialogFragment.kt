package com.lqanh.todoandroid.ui.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.lqanh.todoandroid.R
import com.lqanh.todoandroid.databinding.DialogSetTimeBinding
import java.util.Calendar

class SetTimeDialogFragment : DialogFragment() {

    private var _binding: DialogSetTimeBinding? = null
    private val binding get() = _binding!!

    private var hour = 17
    private var minute = 0
    private var noTime = false
    private var keyboardMode = false
    private var syncing = false
    private var editingHour = true

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        val wrapped = ContextThemeWrapper(requireContext(), R.style.ThemeOverlay_ToDo_TimePickerClock)
        return super.onGetLayoutInflater(savedInstanceState).cloneInContext(wrapped)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogSetTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        lockDialogSize()
        dialog?.window?.setGravity(Gravity.CENTER)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val hasTime = arguments?.getBoolean(ARG_HAS_TIME) == true
        if (hasTime) {
            hour = arguments?.getInt(ARG_HOUR) ?: 17
            minute = arguments?.getInt(ARG_MINUTE) ?: 0
            noTime = false
        } else {
            val now = Calendar.getInstance()
            hour = now.get(Calendar.HOUR_OF_DAY)
            minute = now.get(Calendar.MINUTE)
            noTime = false
        }

        binding.root.alpha = 0f
        syncing = true
        binding.timePicker.setIs24HourView(true)
        applyTimeToPicker()

        binding.tvDigitalHour.setOnClickListener {
            editingHour = true
            setPickerShowingHours(true)
            refreshDigitalDisplay()
        }
        binding.tvDigitalMinute.setOnClickListener {
            editingHour = false
            setPickerShowingHours(false)
            refreshDigitalDisplay()
        }

        binding.btnToggleInput.setOnClickListener { toggleInputMode() }

        binding.etHour.doAfterTextChanged {
            if (syncing || !keyboardMode) return@doAfterTextChanged
            val value = it?.toString()?.toIntOrNull() ?: return@doAfterTextChanged
            if (value in 0..23) {
                hour = value
                noTime = false
                applyTimeToPicker()
                refreshDigitalDisplay()
                updateChips()
            }
        }
        binding.etMinute.doAfterTextChanged {
            if (syncing || !keyboardMode) return@doAfterTextChanged
            val value = it?.toString()?.toIntOrNull() ?: return@doAfterTextChanged
            if (value in 0..59) {
                minute = value
                noTime = false
                applyTimeToPicker()
                refreshDigitalDisplay()
                updateChips()
            }
        }

        binding.chipNoTime.setOnClickListener {
            noTime = true
            updateChips()
        }
        bindPreset(binding.chip0700, 7, 0)
        bindPreset(binding.chip0900, 9, 0)
        bindPreset(binding.chip1000, 10, 0)
        bindPreset(binding.chip1200, 12, 0)
        bindPreset(binding.chip1400, 14, 0)
        bindPreset(binding.chip1600, 16, 0)
        bindPreset(binding.chip1800, 18, 0)

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnDone.setOnClickListener {
            setFragmentResult(
                REQUEST_KEY,
                bundleOf(
                    KEY_NO_TIME to noTime,
                    KEY_HOUR to hour,
                    KEY_MINUTE to minute
                )
            )
            dismiss()
        }

        refreshDigitalDisplay()
        updateChips()
        applyInputModeUi()

        binding.timePicker.post {
            hideSystemChrome(binding.timePicker)
            applyTimeToPicker()
            refreshDigitalDisplay()
            syncing = false
            binding.timePicker.setOnTimeChangedListener { _, h, m ->
                if (syncing || keyboardMode) return@setOnTimeChangedListener
                val hourChanged = h != hour
                val minuteChanged = m != minute
                hour = h
                minute = m
                noTime = false
                when {
                    minuteChanged && !hourChanged -> editingHour = false
                    hourChanged && !minuteChanged -> {
                        // After hour is picked, TimePicker switches to minutes
                        editingHour = false
                    }
                }
                syncEditingFromPicker()
                refreshDigitalDisplay()
                updateChips()
            }
            binding.timePicker.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_UP ||
                    event.actionMasked == MotionEvent.ACTION_CANCEL
                ) {
                    binding.timePicker.post {
                        syncEditingFromPicker()
                        refreshDigitalDisplay()
                    }
                }
                false
            }
            syncEditingFromPicker()
            refreshDigitalDisplay()
            lockDialogSize()
            binding.root.animate().alpha(1f).setDuration(80).start()
            binding.timePicker.post {
                hideSystemChrome(binding.timePicker)
                syncEditingFromPicker()
                refreshDigitalDisplay()
            }
        }
    }

    private fun lockDialogSize() {
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun bindPreset(view: View, h: Int, m: Int) {
        view.setOnClickListener {
            noTime = false
            hour = h
            minute = m
            applyTimeToPicker()
            syncKeyboardFields()
            refreshDigitalDisplay()
            updateChips()
        }
    }

    private fun toggleInputMode() {
        keyboardMode = !keyboardMode
        if (keyboardMode) syncKeyboardFields() else applyTimeToPicker()
        applyInputModeUi()
    }

    private fun applyInputModeUi() {
        binding.timePicker.isVisible = !keyboardMode
        binding.digitalDisplay.isVisible = !keyboardMode
        binding.keyboardPanel.isVisible = keyboardMode
        binding.btnToggleInput.setImageResource(
            if (keyboardMode) R.drawable.ic_schedule else R.drawable.ic_keyboard
        )
        if (keyboardMode) {
            binding.etHour.requestFocus()
            binding.etHour.setSelection(binding.etHour.text?.length ?: 0)
        }
    }

    private fun applyTimeToPicker() {
        val wasSyncing = syncing
        syncing = true
        binding.timePicker.hour = hour
        binding.timePicker.minute = minute
        syncing = wasSyncing
    }

    private fun syncKeyboardFields() {
        val wasSyncing = syncing
        syncing = true
        binding.etHour.setText("%02d".format(hour))
        binding.etMinute.setText("%02d".format(minute))
        syncing = wasSyncing
    }

    private fun refreshDigitalDisplay() {
        binding.tvDigitalHour.text = "%02d".format(hour)
        binding.tvDigitalMinute.text = "%02d".format(minute)
        val active = Color.parseColor("#1E293B")
        val inactive = Color.parseColor("#94A3B8")
        binding.tvDigitalHour.setTextColor(if (editingHour) active else inactive)
        binding.tvDigitalMinute.setTextColor(if (editingHour) inactive else active)
        binding.tvDigitalHour.setTypeface(null, if (editingHour) Typeface.BOLD else Typeface.NORMAL)
        binding.tvDigitalMinute.setTypeface(null, if (editingHour) Typeface.NORMAL else Typeface.BOLD)
    }

    private fun syncEditingFromPicker() {
        try {
            val radial = findViewByClassName(binding.timePicker, "RadialTimePickerView") ?: return
            val showHours = radial.javaClass.declaredFields.firstOrNull { field ->
                field.name.equals("mShowHours", true) ||
                    field.name.equals("showHours", true)
            } ?: return
            showHours.isAccessible = true
            editingHour = showHours.getBoolean(radial)
        } catch (_: Exception) {
        }
    }

    private fun setPickerShowingHours(showHours: Boolean) {
        try {
            val picker = binding.timePicker
            val delegateField = TimePicker::class.java.getDeclaredField("mDelegate")
            delegateField.isAccessible = true
            val delegate = delegateField.get(picker) ?: return
            val method = delegate.javaClass.methods.firstOrNull { method ->
                method.name == "setCurrentItemShowing" && method.parameterTypes.size >= 2
            } ?: return
            val index = if (showHours) 0 else 1
            when (method.parameterTypes.size) {
                2 -> method.invoke(delegate, index, true)
                3 -> method.invoke(delegate, index, true, true)
                else -> method.invoke(delegate, index, true, true, true)
            }
            editingHour = showHours
        } catch (_: Exception) {
            try {
                val radial = findViewByClassName(binding.timePicker, "RadialTimePickerView") ?: return
                val method = radial.javaClass.methods.firstOrNull {
                    it.name.contains("showHours", true) || it.name.contains("ShowHours", true)
                }
                method?.takeIf { it.parameterTypes.size == 1 }?.invoke(radial, showHours)
                editingHour = showHours
            } catch (_: Exception) {
                editingHour = showHours
            }
        }
    }

    private fun updateChips() {
        val presets = listOf(
            binding.chipNoTime to noTime,
            binding.chip0700 to (!noTime && hour == 7 && minute == 0),
            binding.chip0900 to (!noTime && hour == 9 && minute == 0),
            binding.chip1000 to (!noTime && hour == 10 && minute == 0),
            binding.chip1200 to (!noTime && hour == 12 && minute == 0),
            binding.chip1400 to (!noTime && hour == 14 && minute == 0),
            binding.chip1600 to (!noTime && hour == 16 && minute == 0),
            binding.chip1800 to (!noTime && hour == 18 && minute == 0)
        )
        presets.forEach { (chip, selected) -> chip.isSelected = selected }
    }

    private fun hideSystemChrome(root: View) {
        val radial = findViewByClassName(root, "RadialTimePickerView") ?: return

        var current: View? = radial
        while (current != null && current !== root) {
            val parent = current.parent as? ViewGroup ?: break
            for (i in 0 until parent.childCount) {
                val sibling = parent.getChildAt(i)
                if (sibling === current) continue
                if (findViewByClassName(sibling, "RadialTimePickerView") == null) {
                    sibling.visibility = View.GONE
                    sibling.layoutParams = sibling.layoutParams?.apply {
                        height = 0
                        if (this is ViewGroup.MarginLayoutParams) {
                            topMargin = 0
                            bottomMargin = 0
                        }
                    }
                }
            }
            current = parent
        }

        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                val child = root.getChildAt(i)
                if (findViewByClassName(child, "RadialTimePickerView") == null) {
                    child.visibility = View.GONE
                }
            }
        }
    }

    private fun findViewByClassName(root: View, simpleName: String): View? {
        if (root.javaClass.simpleName == simpleName) return root
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                val found = findViewByClassName(root.getChildAt(i), simpleName)
                if (found != null) return found
            }
        }
        return null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SetTimeDialog"
        const val REQUEST_KEY = "set_time_result"
        const val KEY_NO_TIME = "no_time"
        const val KEY_HOUR = "hour"
        const val KEY_MINUTE = "minute"
        private const val ARG_HAS_TIME = "has_time"
        private const val ARG_HOUR = "arg_hour"
        private const val ARG_MINUTE = "arg_minute"

        fun newInstance(hasTime: Boolean, hour: Int, minute: Int): SetTimeDialogFragment {
            return SetTimeDialogFragment().apply {
                arguments = bundleOf(
                    ARG_HAS_TIME to hasTime,
                    ARG_HOUR to hour,
                    ARG_MINUTE to minute
                )
            }
        }
    }
}
