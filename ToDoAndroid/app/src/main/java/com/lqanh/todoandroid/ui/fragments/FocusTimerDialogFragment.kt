package com.lqanh.todoandroid.ui.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.lqanh.todoandroid.data.TaskStatus
import com.lqanh.todoandroid.databinding.DialogFocusTimerBinding
import com.lqanh.todoandroid.ui.DialogSizing
import com.lqanh.todoandroid.ui.TaskViewModel

// Hộp thoại đếm giờ tập trung (Pomodoro 25 phút) cho một task.
class FocusTimerDialogFragment : DialogFragment() {

    private var _binding: DialogFocusTimerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by activityViewModels()

    private val taskId get() = requireArguments().getString(ARG_TASK_ID).orEmpty()

    private var remainingMillis = SESSION_MILLIS
    private var endAtElapsed = 0L
    private var running = false
    private var finished = false
    private var timer: CountDownTimer? = null

    // Tạo dialog với nền trong suốt.
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    // Tạo view từ layout dialog_focus_timer.xml.
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogFocusTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Đặt kích thước dialog rộng 92% màn hình và căn giữa.
    override fun onStart() {
        super.onStart()
        DialogSizing.apply(dialog)
    }

    // Khôi phục phiên đang chạy (nếu xoay màn hình), gắn sự kiện cho các nút.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvFocusTask.text = requireArguments().getString(ARG_TASK_TITLE).orEmpty()

        savedInstanceState?.let {
            remainingMillis = it.getLong(STATE_REMAINING, SESSION_MILLIS)
            endAtElapsed = it.getLong(STATE_END_AT)
            running = it.getBoolean(STATE_RUNNING)
            finished = it.getBoolean(STATE_FINISHED)
        }
        if (running) {
            remainingMillis = (endAtElapsed - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
            if (remainingMillis == 0L) onSessionFinished(playSound = false) else startTimer()
        }

        binding.btnFocusStart.setOnClickListener { if (running) pause() else start() }
        binding.btnFocusReset.setOnClickListener { reset() }
        binding.btnFocusClose.setOnClickListener { dismiss() }
        binding.btnFocusMarkDone.setOnClickListener {
            viewModel.changeTaskStatus(taskId, TaskStatus.DONE)
            dismiss()
        }
        render()
    }

    // Bắt đầu/tiếp tục đếm. Task chưa làm sẽ được chuyển sang DOING.
    private fun start() {
        if (finished) reset()
        val task = viewModel.uiState.value.tasks.firstOrNull { it.id == taskId }
        if (task?.status == TaskStatus.TODO) viewModel.changeTaskStatus(taskId, TaskStatus.DOING)
        running = true
        endAtElapsed = SystemClock.elapsedRealtime() + remainingMillis
        startTimer()
        render()
    }

    // Tạm dừng, giữ lại thời gian còn lại.
    private fun pause() {
        timer?.cancel()
        running = false
        remainingMillis = (endAtElapsed - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
        render()
    }

    // Dừng và đưa đồng hồ về 25:00.
    private fun reset() {
        timer?.cancel()
        running = false
        finished = false
        remainingMillis = SESSION_MILLIS
        render()
    }

    // Chạy CountDownTimer tới endAtElapsed, cập nhật giao diện mỗi 250ms.
    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(remainingMillis, TICK_MILLIS) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = (endAtElapsed - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
                render()
            }

            override fun onFinish() {
                onSessionFinished(playSound = true)
            }
        }.start()
    }

    // Kết thúc phiên: phát âm báo, hiện nút đánh dấu hoàn thành.
    private fun onSessionFinished(playSound: Boolean) {
        timer?.cancel()
        running = false
        finished = true
        remainingMillis = 0L
        if (playSound) {
            runCatching {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                RingtoneManager.getRingtone(requireContext(), uri)?.play()
            }
        }
        render()
    }

    // Vẽ lại thời gian, vòng tiến độ, trạng thái và các nút.
    private fun render() {
        val b = _binding ?: return
        val totalSeconds = ((remainingMillis + 999) / 1000).toInt()
        b.tvFocusTime.text = "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
        b.progressFocus.progress = (remainingMillis * 1000 / SESSION_MILLIS).toInt()
        b.tvFocusState.text = when {
            finished -> "Session complete"
            running -> "Focusing"
            remainingMillis < SESSION_MILLIS -> "Paused"
            else -> "Ready"
        }
        b.btnFocusStart.text = when {
            running -> "Pause"
            finished -> "Start again"
            remainingMillis < SESSION_MILLIS -> "Resume"
            else -> "Start"
        }
        b.btnFocusMarkDone.isVisible = finished
        val window = dialog?.window ?: return
        if (running) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Lưu trạng thái đồng hồ để khôi phục khi xoay màn hình.
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(STATE_REMAINING, remainingMillis)
        outState.putLong(STATE_END_AT, endAtElapsed)
        outState.putBoolean(STATE_RUNNING, running)
        outState.putBoolean(STATE_FINISHED, finished)
    }

    // Hủy đồng hồ và giải phóng binding khi view bị hủy.
    override fun onDestroyView() {
        timer?.cancel()
        timer = null
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FocusTimerDialog"

        private const val SESSION_MILLIS = 25 * 60 * 1000L
        private const val TICK_MILLIS = 250L

        private const val ARG_TASK_ID = "arg_task_id"
        private const val ARG_TASK_TITLE = "arg_task_title"
        private const val STATE_REMAINING = "state_remaining"
        private const val STATE_END_AT = "state_end_at"
        private const val STATE_RUNNING = "state_running"
        private const val STATE_FINISHED = "state_finished"

        // Tạo dialog Focus cho task được chọn.
        fun newInstance(taskId: String, title: String): FocusTimerDialogFragment {
            return FocusTimerDialogFragment().apply {
                arguments = bundleOf(ARG_TASK_ID to taskId, ARG_TASK_TITLE to title)
            }
        }
    }
}
