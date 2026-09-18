package com.lqanh.todoandroid.ui.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.lqanh.todoandroid.R
import com.lqanh.todoandroid.databinding.DialogSetTimeBinding

class SetTimeDialogFragment : DialogFragment() {

    private var _binding: DialogSetTimeBinding? = null
    private val binding get() = _binding!!

    private var hour = 17
    private var minute = 9
    private var noTime = true
    private var keyboardMode = false
    private var syncing = false

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
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setGravity(Gravity.CENTER)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val hasTime = arguments?.getBoolean(ARG_HAS_TIME) == true
        if (hasTime) {
            hour = arguments?.getInt(ARG_HOUR) ?: 17
            minute = arguments?.getInt(ARG_MINUTE) ?: 0
            noTime = false
        }

        binding.timePicker.setIs24HourView(true)
        applyTimeToPicker()
        binding.timePicker.post { hideSystemModeToggle(binding.timePicker) }

        binding.timePicker.setOnTimeChangedListener { _, h, m ->
            if (syncing || keyboardMode) return@setOnTimeChangedListener
            hour = h
            minute = m
            noTime = false
            updateChips()
        }

        binding.btnToggleInput.setOnClickListener { toggleInputMode() }

        binding.etHour.doAfterTextChanged {
            if (syncing || !keyboardMode) return@doAfterTextChanged
            val value = it?.toString()?.toIntOrNull() ?: return@doAfterTextChanged
            if (value in 0..23) {
                hour = value
                noTime = false
                applyTimeToPicker()
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

        updateChips()
        applyInputModeUi()
    }

    private fun bindPreset(view: View, h: Int, m: Int) {
        view.setOnClickListener {
            noTime = false
            hour = h
            minute = m
            applyTimeToPicker()
            syncKeyboardFields()
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
        syncing = true
        binding.timePicker.hour = hour
        binding.timePicker.minute = minute
        syncing = false
    }

    private fun syncKeyboardFields() {
        syncing = true
        binding.etHour.setText("%02d".format(hour))
        binding.etMinute.setText("%02d".format(minute))
        syncing = false
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

    private fun hideSystemModeToggle(root: View) {
        val names = listOf(
            "toggle_mode",
            "material_timepicker_mode_button",
            "input_mode",
            "keyboard_mode"
        )
        names.forEach { name ->
            val id = resources.getIdentifier(name, "id", "android")
            if (id != 0) root.findViewById<View>(id)?.isVisible = false
            val appId = resources.getIdentifier(name, "id", requireContext().packageName)
            if (appId != 0) root.findViewById<View>(appId)?.isVisible = false
        }
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                val child = root.getChildAt(i)
                if ((child is ImageButton || child is ImageView) &&
                    child !== binding.btnToggleInput &&
                    child.contentDescription?.toString()?.contains("keyboard", true) == true
                ) {
                    child.isVisible = false
                }
                hideSystemModeToggle(child)
            }
        }
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
