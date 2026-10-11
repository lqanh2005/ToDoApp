package com.lqanh.todoandroid.ui

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import kotlin.math.min

// Tính kích thước hộp thoại theo màn hình hiện tại để hiển thị vừa trên mọi máy dọc (điện thoại nhỏ tới máy tính bảng).
object DialogSizing {

    private const val WIDTH_RATIO = 0.92f
    private const val MAX_WIDTH_DP = 480f
    private const val MAX_HEIGHT_RATIO = 0.9f

    // Chiều rộng hộp thoại: 92% màn hình nhưng không quá 480dp.
    fun dialogWidthPx(context: Context): Int {
        val metrics = context.resources.displayMetrics
        return min(metrics.widthPixels * WIDTH_RATIO, MAX_WIDTH_DP * metrics.density).toInt()
    }

    // Chiều cao tối đa của hộp thoại: 90% màn hình.
    fun maxDialogHeightPx(context: Context): Int =
        (context.resources.displayMetrics.heightPixels * MAX_HEIGHT_RATIO).toInt()

    // Áp kích thước cho cửa sổ hộp thoại và căn giữa. Nội dung dài hơn chiều cao tối đa sẽ cuộn được.
    fun apply(dialog: Dialog?) {
        val window = dialog?.window ?: return
        val context = dialog.context
        window.setLayout(dialogWidthPx(context), ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER)
        val content = window.decorView.findViewById<ViewGroup>(android.R.id.content)?.getChildAt(0)
        content?.post {
            val maxHeight = maxDialogHeightPx(context)
            if (content.height > maxHeight) {
                window.setLayout(dialogWidthPx(context), maxHeight)
            }
        }
    }
}
