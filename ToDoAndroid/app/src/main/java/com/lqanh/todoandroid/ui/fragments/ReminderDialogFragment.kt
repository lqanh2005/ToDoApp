package com.lqanh.todoandroid.ui.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.lqanh.todoandroid.R
import com.lqanh.todoandroid.databinding.DialogReminderBinding

class ReminderDialogFragment : DialogFragment() {

    private var _binding: DialogReminderBinding? = null
    private val binding get() = _binding!!

    private var selectedKey = OPTION_5_MIN
    private var customLabel: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogReminderBinding.inflate(inflater, container, false)
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
        val enabled = arguments?.getBoolean(ARG_ENABLED, true) != false
        selectedKey = arguments?.getString(ARG_KEY) ?: OPTION_5_MIN
        customLabel = arguments?.getString(ARG_CUSTOM)?.takeIf { it.isNotBlank() }

        binding.switchReminder.isChecked = enabled
        applyEnabledState(enabled)
        syncChecks()

        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            applyEnabledState(isChecked)
            if (isChecked && selectedKey.isBlank()) {
                selectedKey = OPTION_5_MIN
                syncChecks()
            }
        }

        bindExclusive(binding.cbAtTaskTime, OPTION_AT_TASK)
        bindExclusive(binding.cb5Min, OPTION_5_MIN)
        bindExclusive(binding.cb15Min, OPTION_15_MIN)
        bindExclusive(binding.cb30Min, OPTION_30_MIN)
        bindExclusive(binding.cb1Day, OPTION_1_DAY)
        binding.cbCustomize.setOnClickListener {
            if (!binding.switchReminder.isChecked) return@setOnClickListener
            showCustomizePicker()
        }

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnDone.setOnClickListener {
            val on = binding.switchReminder.isChecked
            setFragmentResult(
                REQUEST_KEY,
                bundleOf(
                    KEY_ENABLED to on,
                    KEY_VALUE to if (on) selectedKey else "",
                    KEY_LABEL to if (on) displayLabelFor(selectedKey) else "",
                    KEY_CUSTOM_LABEL to (customLabel ?: "")
                )
            )
            dismiss()
        }
    }

    private fun bindExclusive(box: MaterialCheckBox, key: String) {
        box.setOnClickListener {
            if (!binding.switchReminder.isChecked) {
                box.isChecked = false
                return@setOnClickListener
            }
            selectedKey = key
            syncChecks()
        }
    }

    private fun showCustomizePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(9)
            .setMinute(0)
            .setTitleText("Customize time")
            .build()
        picker.addOnPositiveButtonClickListener {
            customLabel = "%02d:%02d".format(picker.hour, picker.minute)
            selectedKey = OPTION_CUSTOM
            syncChecks()
        }
        picker.addOnNegativeButtonClickListener { syncChecks() }
        picker.show(parentFragmentManager, "reminder_custom_time")
    }

    private fun applyEnabledState(enabled: Boolean) {
        binding.reminderOptions.alpha = if (enabled) 1f else 0.45f
        listOf(
            binding.cbAtTaskTime,
            binding.cb5Min,
            binding.cb15Min,
            binding.cb30Min,
            binding.cb1Day,
            binding.cbCustomize
        ).forEach { it.isEnabled = enabled }
    }

    private fun syncChecks() {
        val map = listOf(
            binding.cbAtTaskTime to OPTION_AT_TASK,
            binding.cb5Min to OPTION_5_MIN,
            binding.cb15Min to OPTION_15_MIN,
            binding.cb30Min to OPTION_30_MIN,
            binding.cb1Day to OPTION_1_DAY,
            binding.cbCustomize to OPTION_CUSTOM
        )
        val active = ContextCompat.getColor(requireContext(), R.color.primary)
        val normal = ContextCompat.getColor(requireContext(), R.color.slate_700)
        map.forEach { (box, key) ->
            val selected = selectedKey == key
            box.isChecked = selected
            box.setTextColor(if (selected) active else normal)
            if (key == OPTION_CUSTOM) {
                box.text = if (customLabel != null) "Customize time ($customLabel)" else "Customize time"
            }
        }
    }

    private fun displayLabelFor(key: String): String = when (key) {
        OPTION_AT_TASK -> "At task time"
        OPTION_5_MIN -> "5 min"
        OPTION_15_MIN -> "15 min"
        OPTION_30_MIN -> "30 min"
        OPTION_1_DAY -> "1 day"
        OPTION_CUSTOM -> customLabel ?: "Custom"
        else -> "5 min"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ReminderDialog"
        const val REQUEST_KEY = "reminder_result"
        const val KEY_ENABLED = "enabled"
        const val KEY_VALUE = "value"
        const val KEY_LABEL = "label"
        const val KEY_CUSTOM_LABEL = "custom_label"

        const val OPTION_AT_TASK = "at_task"
        const val OPTION_5_MIN = "5_min"
        const val OPTION_15_MIN = "15_min"
        const val OPTION_30_MIN = "30_min"
        const val OPTION_1_DAY = "1_day"
        const val OPTION_CUSTOM = "custom_time"

        private const val ARG_ENABLED = "arg_enabled"
        private const val ARG_KEY = "arg_key"
        private const val ARG_CUSTOM = "arg_custom"

        fun newInstance(enabled: Boolean, key: String?, custom: String?): ReminderDialogFragment {
            return ReminderDialogFragment().apply {
                arguments = bundleOf(
                    ARG_ENABLED to enabled,
                    ARG_KEY to (key ?: OPTION_5_MIN),
                    ARG_CUSTOM to (custom ?: "")
                )
            }
        }
    }
}
