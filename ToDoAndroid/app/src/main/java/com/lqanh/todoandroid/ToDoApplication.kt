package com.lqanh.todoandroid

import android.app.Application
import com.lqanh.todoandroid.data.SettingsRepository

// Lớp Application chạy đầu tiên khi mở app, dùng để khởi tạo cấu hình chung.
class ToDoApplication : Application() {

    // Áp dụng theme + ngôn ngữ đã lưu trước khi Activity đầu tiên được tạo, tránh màn hình bị nháy đổi màu.
    override fun onCreate() {
        super.onCreate()
        SettingsRepository(this).applyAll()
    }
}
