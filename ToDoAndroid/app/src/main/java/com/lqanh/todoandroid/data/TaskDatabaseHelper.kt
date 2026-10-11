package com.lqanh.todoandroid.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Quản lý file database SQLite todo.db: tạo bảng, nâng cấp phiên bản và cung cấp kết nối.
class TaskDatabaseHelper private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    private val appContext = context.applicationContext

    // Bật ràng buộc khóa ngoại để khi xóa task thì subtask/ảnh của nó cũng bị xóa theo (ON DELETE CASCADE).
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // Chạy đúng 1 lần khi file database chưa tồn tại: tạo bảng rồi nạp dữ liệu ban đầu.
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(TaskContract.SQL_CREATE_TASKS)
        db.execSQL(TaskContract.SQL_CREATE_SUBTASKS)
        db.execSQL(TaskContract.SQL_CREATE_IMAGES)
        TaskDao.replaceAll(db, loadLegacyTasks() ?: InitialTasks.list)
    }

    // Chạy khi tăng DATABASE_VERSION. Version 2 thêm cột is_starred, giữ nguyên dữ liệu cũ.
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion == 1 && newVersion >= 2) {
            db.execSQL(TaskContract.SQL_ADD_IS_STARRED)
            return
        }
        db.execSQL("DROP TABLE IF EXISTS ${TaskContract.Images.TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${TaskContract.Subtasks.TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${TaskContract.Tasks.TABLE}")
        onCreate(db)
    }

    // Đọc danh sách task từ bản cũ (JSON trong SharedPreferences) để chuyển sang SQLite, sau đó xóa dữ liệu cũ.
    private fun loadLegacyTasks(): List<Task>? {
        val prefs = appContext.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(LEGACY_KEY_TASKS, null) ?: return null
        val tasks = runCatching {
            val type = object : TypeToken<List<Task>>() {}.type
            Gson().fromJson<List<Task>>(json, type)
        }.getOrNull()
        prefs.edit().remove(LEGACY_KEY_TASKS).apply()
        return tasks
    }

    companion object {
        private const val DATABASE_NAME = "todo.db"
        private const val DATABASE_VERSION = 2

        private const val LEGACY_PREFS_NAME = "nhipsang_plan"
        private const val LEGACY_KEY_TASKS = "nhipsang_plan_tasks_v1"

        @Volatile
        private var instance: TaskDatabaseHelper? = null

        // Trả về một instance duy nhất cho toàn app, tránh mở nhiều kết nối tới cùng một file database.
        fun getInstance(context: Context): TaskDatabaseHelper =
            instance ?: synchronized(this) {
                instance ?: TaskDatabaseHelper(context).also { instance = it }
            }
    }
}
