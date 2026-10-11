package com.lqanh.todoandroid.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

// Chế độ giao diện: theo hệ thống, sáng, tối.
enum class ThemeMode { SYSTEM, LIGHT, DARK }

// Đọc/ghi cài đặt người dùng bằng SharedPreferences.
class SettingsRepository(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Chế độ giao diện đã lưu (mặc định theo hệ thống).
    var themeMode: ThemeMode
        get() = enumOrDefault(prefs.getString(KEY_THEME, null), ThemeMode.SYSTEM)
        set(value) = prefs.edit().putString(KEY_THEME, value.name).apply()

    // Bật/tắt rung khi thao tác (mặc định bật).
    var hapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC, value).apply()

    // Áp dụng theme đã lưu và cố định ngôn ngữ tiếng Anh cho toàn app. Gọi khi app khởi động.
    fun applyAll() {
        applyTheme(themeMode)
        applyEnglish()
    }

    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_HAPTIC = "haptic_enabled"

        // Chuyển giao diện sáng/tối/theo hệ thống. AppCompat sẽ tự vẽ lại các Activity đang mở.
        fun applyTheme(mode: ThemeMode) {
            val nightMode = when (mode) {
                ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            }
            if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
                AppCompatDelegate.setDefaultNightMode(nightMode)
            }
        }

        // Luôn dùng tiếng Anh, kể cả khi máy đặt ngôn ngữ khác (ảnh hưởng cả định dạng ngày giờ).
        private fun applyEnglish() {
            val locales = LocaleListCompat.forLanguageTags("en")
            if (AppCompatDelegate.getApplicationLocales() != locales) {
                AppCompatDelegate.setApplicationLocales(locales)
            }
        }

        // Chuyển chuỗi đã lưu thành enum; chuỗi rỗng hoặc sai thì trả về giá trị mặc định.
        private inline fun <reified T : Enum<T>> enumOrDefault(value: String?, default: T): T =
            value?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default
    }
}
