package com.lqanh.todoandroid.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.lqanh.todoandroid.data.TaskContract.Images
import com.lqanh.todoandroid.data.TaskContract.Subtasks
import com.lqanh.todoandroid.data.TaskContract.Tasks

// Các thao tác đọc/ghi dữ liệu task trực tiếp với SQLite.
object TaskDao {

    // Đọc toàn bộ task (kèm subtask và ảnh) theo đúng thứ tự đã lưu.
    fun readAll(db: SQLiteDatabase): List<Task> {
        val subtasksByTask = readSubtasks(db)
        val imagesByTask = readImages(db)
        val result = mutableListOf<Task>()
        db.query(Tasks.TABLE, null, null, null, null, null, "${Tasks.POSITION} ASC").use { c ->
            while (c.moveToNext()) {
                val id = c.string(Tasks.ID)
                result += Task(
                    id = id,
                    code = c.string(Tasks.CODE),
                    sprint = c.stringOrNull(Tasks.SPRINT),
                    title = c.string(Tasks.TITLE),
                    description = c.stringOrNull(Tasks.DESCRIPTION),
                    status = enumOrDefault(c.string(Tasks.STATUS), TaskStatus.TODO),
                    priority = enumOrDefault(c.string(Tasks.PRIORITY), PriorityLevel.TB),
                    category = c.string(Tasks.CATEGORY),
                    categoryLabel = c.string(Tasks.CATEGORY_LABEL),
                    categoryEmoji = c.stringOrNull(Tasks.CATEGORY_EMOJI),
                    dueDate = c.string(Tasks.DUE_DATE),
                    dueTime = c.string(Tasks.DUE_TIME),
                    dueAtMillis = c.longOrNull(Tasks.DUE_AT_MILLIS),
                    startAtMillis = c.longOrNull(Tasks.START_AT_MILLIS),
                    endAtMillis = c.longOrNull(Tasks.END_AT_MILLIS),
                    awaitingCompletion = c.bool(Tasks.AWAITING_COMPLETION),
                    isOverdue = c.bool(Tasks.IS_OVERDUE),
                    overdueText = c.stringOrNull(Tasks.OVERDUE_TEXT),
                    subtasks = subtasksByTask[id].orEmpty(),
                    attachmentsCount = c.intOrNull(Tasks.ATTACHMENTS_COUNT),
                    notes = c.stringOrNull(Tasks.NOTES),
                    location = c.stringOrNull(Tasks.LOCATION),
                    reminder = c.string(Tasks.REMINDER),
                    progress = c.intOrNull(Tasks.PROGRESS),
                    images = imagesByTask[id],
                    spec = c.stringOrNull(Tasks.SPEC),
                    completedAt = c.stringOrNull(Tasks.COMPLETED_AT)
                )
            }
        }
        return result
    }

    // Ghi đè toàn bộ dữ liệu bằng danh sách mới trong một transaction (lỗi giữa chừng thì không mất dữ liệu cũ).
    fun replaceAll(db: SQLiteDatabase, tasks: List<Task>) {
        db.beginTransaction()
        try {
            db.delete(Images.TABLE, null, null)
            db.delete(Subtasks.TABLE, null, null)
            db.delete(Tasks.TABLE, null, null)
            tasks.forEachIndexed { index, task -> insert(db, task, index) }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // Thêm mới hoặc cập nhật một task. Task mới được đặt lên đầu danh sách.
    fun upsert(db: SQLiteDatabase, task: Task) {
        db.beginTransaction()
        try {
            val position = findPosition(db, task.id) ?: (minPosition(db) - 1)
            db.delete(Tasks.TABLE, "${Tasks.ID} = ?", arrayOf(task.id))
            insert(db, task, position)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // Xóa một task theo id; subtask và ảnh bị xóa theo nhờ ON DELETE CASCADE.
    fun delete(db: SQLiteDatabase, taskId: String) {
        db.delete(Tasks.TABLE, "${Tasks.ID} = ?", arrayOf(taskId))
    }

    // Chèn một task cùng toàn bộ việc con và ảnh của nó vào database.
    private fun insert(db: SQLiteDatabase, task: Task, position: Int) {
        db.insertWithOnConflict(
            Tasks.TABLE, null, toValues(task, position), SQLiteDatabase.CONFLICT_REPLACE
        )
        task.subtasks.forEachIndexed { index, subtask ->
            db.insert(Subtasks.TABLE, null, ContentValues().apply {
                put(Subtasks.ID, subtask.id)
                put(Subtasks.TASK_ID, task.id)
                put(Subtasks.POSITION, index)
                put(Subtasks.TITLE, subtask.title)
                put(Subtasks.COMPLETED, if (subtask.completed) 1 else 0)
            })
        }
        task.images?.forEachIndexed { index, image ->
            db.insert(Images.TABLE, null, ContentValues().apply {
                put(Images.TASK_ID, task.id)
                put(Images.POSITION, index)
                put(Images.URL, image.url)
                put(Images.CAPTION, image.caption)
            })
        }
    }

    // Chuyển đối tượng Task thành ContentValues (cặp cột - giá trị) để ghi vào bảng tasks.
    private fun toValues(task: Task, position: Int) = ContentValues().apply {
        put(Tasks.ID, task.id)
        put(Tasks.POSITION, position)
        put(Tasks.CODE, task.code)
        put(Tasks.SPRINT, task.sprint)
        put(Tasks.TITLE, task.title)
        put(Tasks.DESCRIPTION, task.description)
        put(Tasks.STATUS, task.status.name)
        put(Tasks.PRIORITY, task.priority.name)
        put(Tasks.CATEGORY, task.category)
        put(Tasks.CATEGORY_LABEL, task.categoryLabel)
        put(Tasks.CATEGORY_EMOJI, task.categoryEmoji)
        put(Tasks.DUE_DATE, task.dueDate)
        put(Tasks.DUE_TIME, task.dueTime)
        put(Tasks.DUE_AT_MILLIS, task.dueAtMillis)
        put(Tasks.START_AT_MILLIS, task.startAtMillis)
        put(Tasks.END_AT_MILLIS, task.endAtMillis)
        put(Tasks.AWAITING_COMPLETION, if (task.awaitingCompletion) 1 else 0)
        put(Tasks.IS_OVERDUE, if (task.isOverdue) 1 else 0)
        put(Tasks.OVERDUE_TEXT, task.overdueText)
        put(Tasks.ATTACHMENTS_COUNT, task.attachmentsCount)
        put(Tasks.NOTES, task.notes)
        put(Tasks.LOCATION, task.location)
        put(Tasks.REMINDER, task.reminder)
        put(Tasks.PROGRESS, task.progress)
        put(Tasks.SPEC, task.spec)
        put(Tasks.COMPLETED_AT, task.completedAt)
    }

    // Đọc tất cả việc con và nhóm theo task_id.
    private fun readSubtasks(db: SQLiteDatabase): Map<String, List<Subtask>> {
        val map = mutableMapOf<String, MutableList<Subtask>>()
        db.query(
            Subtasks.TABLE, null, null, null, null, null,
            "${Subtasks.TASK_ID} ASC, ${Subtasks.POSITION} ASC"
        ).use { c ->
            while (c.moveToNext()) {
                map.getOrPut(c.string(Subtasks.TASK_ID)) { mutableListOf() } += Subtask(
                    id = c.string(Subtasks.ID),
                    title = c.string(Subtasks.TITLE),
                    completed = c.bool(Subtasks.COMPLETED)
                )
            }
        }
        return map
    }

    // Đọc tất cả ảnh đính kèm và nhóm theo task_id.
    private fun readImages(db: SQLiteDatabase): Map<String, List<TaskImage>> {
        val map = mutableMapOf<String, MutableList<TaskImage>>()
        db.query(
            Images.TABLE, null, null, null, null, null,
            "${Images.TASK_ID} ASC, ${Images.POSITION} ASC"
        ).use { c ->
            while (c.moveToNext()) {
                map.getOrPut(c.string(Images.TASK_ID)) { mutableListOf() } += TaskImage(
                    url = c.string(Images.URL),
                    caption = c.string(Images.CAPTION)
                )
            }
        }
        return map
    }

    // Tìm vị trí hiện tại của task trong danh sách; null nếu task chưa có.
    private fun findPosition(db: SQLiteDatabase, taskId: String): Int? =
        db.query(
            Tasks.TABLE, arrayOf(Tasks.POSITION), "${Tasks.ID} = ?", arrayOf(taskId),
            null, null, null
        ).use { c -> if (c.moveToFirst()) c.getInt(0) else null }

    // Lấy vị trí nhỏ nhất đang có, dùng để chèn task mới lên đầu.
    private fun minPosition(db: SQLiteDatabase): Int =
        db.rawQuery("SELECT MIN(${Tasks.POSITION}) FROM ${Tasks.TABLE}", null).use { c ->
            if (c.moveToFirst() && !c.isNull(0)) c.getInt(0) else 0
        }

    // Chuyển chuỗi trong database thành enum; sai giá trị thì trả về mặc định.
    private inline fun <reified T : Enum<T>> enumOrDefault(value: String, default: T): T =
        runCatching { enumValueOf<T>(value) }.getOrDefault(default)

    // Đọc cột dạng chuỗi, null thì trả về chuỗi rỗng.
    private fun Cursor.string(column: String): String =
        getString(getColumnIndexOrThrow(column)).orEmpty()

    // Đọc cột dạng chuỗi, giữ nguyên null.
    private fun Cursor.stringOrNull(column: String): String? {
        val index = getColumnIndexOrThrow(column)
        return if (isNull(index)) null else getString(index)
    }

    // Đọc cột dạng số Long, giữ nguyên null.
    private fun Cursor.longOrNull(column: String): Long? {
        val index = getColumnIndexOrThrow(column)
        return if (isNull(index)) null else getLong(index)
    }

    // Đọc cột dạng số Int, giữ nguyên null.
    private fun Cursor.intOrNull(column: String): Int? {
        val index = getColumnIndexOrThrow(column)
        return if (isNull(index)) null else getInt(index)
    }

    // Đọc cột 0/1 thành Boolean.
    private fun Cursor.bool(column: String): Boolean =
        getInt(getColumnIndexOrThrow(column)) != 0
}
