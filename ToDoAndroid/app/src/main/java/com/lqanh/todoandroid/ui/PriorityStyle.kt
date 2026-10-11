package com.lqanh.todoandroid.ui

import android.graphics.Color
import com.lqanh.todoandroid.data.PriorityLevel

// Nhãn, màu và điểm số của từng mức ưu tiên, dùng chung cho màn tạo task, thẻ task và bộ lọc.
object PriorityStyle {

    val ordered = listOf(PriorityLevel.CAO, PriorityLevel.TB, PriorityLevel.THAP)

    // Tên hiển thị của mức ưu tiên.
    fun label(level: PriorityLevel): String = when (level) {
        PriorityLevel.CAO -> "High"
        PriorityLevel.TB -> "Medium"
        PriorityLevel.THAP -> "Low"
    }

    // Màu cờ của mức ưu tiên: cao đỏ, trung bình cam, thấp xanh.
    fun color(level: PriorityLevel): Int = when (level) {
        PriorityLevel.CAO -> Color.parseColor("#EF4444")
        PriorityLevel.TB -> Color.parseColor("#F59E0B")
        PriorityLevel.THAP -> Color.parseColor("#3B82F6")
    }

    // Điểm dùng để sắp xếp: càng cao càng ưu tiên.
    fun score(level: PriorityLevel): Int = when (level) {
        PriorityLevel.CAO -> 3
        PriorityLevel.TB -> 2
        PriorityLevel.THAP -> 1
    }
}
