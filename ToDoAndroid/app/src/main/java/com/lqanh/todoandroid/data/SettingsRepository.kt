package com.lqanh.todoandroid.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

// Chế độ giao diện: theo hệ thống, sáng, tối.
enum class ThemeMode { SYSTEM, LIGHT, DARK }

// Ngôn ngữ của app kèm mã ngôn ngữ (tag) tương ứng.
enum class AppLanguage(val tag: String) { SYSTEM(""), VIETNAMESE("vi"), ENGLISH("en") }

// Đọc/ghi cài đặt người dùng bằng SharedPreferences.
class SettingsRepository(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Chế độ giao diện đã lưu (mặc định theo hệ thống).
    var themeMode: ThemeMode
        get() = enumOrDefault(prefs.getString(KEY_THEME, null), ThemeMode.SYSTEM)
        set(value) = prefs.edit().putString(KEY_THEME, value.name).apply()

    // Ngôn ngữ đã lưu (mặc định theo hệ thống).
    var language: AppLanguage
        get() = enumOrDefault(prefs.getString(KEY_LANGUAGE, null), AppLanguage.SYSTEM)
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value.name).apply()

    // Bật/tắt rung khi thao tác (mặc định bật).
    var hapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC, value).apply()

    // Áp dụng theme và ngôn ngữ đã lưu cho toàn app. Gọi khi app khởi động hoặc khi người dùng đổi cài đặt.
    fun applyAll() {
        applyTheme(themeMode)
        applyLanguage(language)
    }

    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_LANGUAGE = "language"
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

        // Đổi ngôn ngữ riêng cho app (SYSTEM = dùng ngôn ngữ của máy).
        fun applyLanguage(language: AppLanguage) {
            val locales = if (language == AppLanguage.SYSTEM) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(language.tag)
            }
            if (AppCompatDelegate.getApplicationLocales() != locales) {
                AppCompatDelegate.setApplicationLocales(locales)
            }
        }

        // Chuyển chuỗi đã lưu thành enum; chuỗi rỗng hoặc sai thì trả về giá trị mặc định.
        private inline fun <reified T : Enum<T>> enumOrDefault(value: String?, default: T): T =
            value?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default
    }
}
