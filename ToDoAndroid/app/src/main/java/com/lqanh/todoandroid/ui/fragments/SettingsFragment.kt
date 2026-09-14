package com.lqanh.todoandroid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lqanh.todoandroid.R
import com.lqanh.todoandroid.databinding.FragmentSettingsBinding
import com.lqanh.todoandroid.ui.TaskViewModel
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.etName.setText("")
        binding.etEmail.setText("")

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etName.text?.toString().orEmpty().trim()
            val email = binding.etEmail.text?.toString().orEmpty().trim()
            binding.tvProfileName.text = name.ifBlank { "Chưa cập nhật tên" }
            binding.tvProfileEmail.text = email.ifBlank { "Chưa cập nhật email" }
            binding.tvProfileName.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (name.isBlank()) R.color.outline else R.color.on_surface
                )
            )
            Toast.makeText(requireContext(), "Đã lưu thay đổi thành công!", Toast.LENGTH_SHORT).show()
        }

        binding.btnReset.setOnClickListener { viewModel.resetData() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (binding.switchHaptic.isChecked != state.hapticEnabled) {
                        binding.switchHaptic.isChecked = state.hapticEnabled
                    }
                }
            }
        }

        binding.switchHaptic.setOnCheckedChangeListener { _, checked ->
            viewModel.setHapticEnabled(checked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
