package com.lqanh.todoandroid.data

// Định nghĩa tên bảng, tên cột và câu lệnh tạo bảng SQLite.
object TaskContract {

    // Bảng tasks: mỗi dòng là một công việc.
    object Tasks {
        const val TABLE = "tasks"
        const val ID = "id"
        const val POSITION = "position"
        const val CODE = "code"
        const val SPRINT = "sprint"
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val STATUS = "status"
        const val PRIORITY = "priority"
        const val CATEGORY = "category"
        const val CATEGORY_LABEL = "category_label"
        const val CATEGORY_EMOJI = "category_emoji"
        const val DUE_DATE = "due_date"
        const val DUE_TIME = "due_time"
        const val DUE_AT_MILLIS = "due_at_millis"
        const val START_AT_MILLIS = "start_at_millis"
        const val END_AT_MILLIS = "end_at_millis"
        const val AWAITING_COMPLETION = "awaiting_completion"
        const val IS_OVERDUE = "is_overdue"
        const val OVERDUE_TEXT = "overdue_text"
        const val ATTACHMENTS_COUNT = "attachments_count"
        const val NOTES = "notes"
        const val LOCATION = "location"
        const val REMINDER = "reminder"
        const val PROGRESS = "progress"
        const val SPEC = "spec"
        const val COMPLETED_AT = "completed_at"
        const val IS_STARRED = "is_starred"
    }

    // Bảng subtasks: việc con, liên kết với tasks qua task_id.
    object Subtasks {
        const val TABLE = "subtasks"
        const val ID = "id"
        const val TASK_ID = "task_id"
        const val POSITION = "position"
        const val TITLE = "title"
        const val COMPLETED = "completed"
    }

    // Bảng task_images: ảnh đính kèm, liên kết với tasks qua task_id.
    object Images {
        const val TABLE = "task_images"
        const val TASK_ID = "task_id"
        const val POSITION = "position"
        const val URL = "url"
        const val CAPTION = "caption"
    }

    const val SQL_CREATE_TASKS = """
        CREATE TABLE ${Tasks.TABLE} (
            ${Tasks.ID} TEXT PRIMARY KEY NOT NULL,
            ${Tasks.POSITION} INTEGER NOT NULL,
            ${Tasks.CODE} TEXT,
            ${Tasks.SPRINT} TEXT,
            ${Tasks.TITLE} TEXT,
            ${Tasks.DESCRIPTION} TEXT,
            ${Tasks.STATUS} TEXT,
            ${Tasks.PRIORITY} TEXT,
            ${Tasks.CATEGORY} TEXT,
            ${Tasks.CATEGORY_LABEL} TEXT,
            ${Tasks.CATEGORY_EMOJI} TEXT,
            ${Tasks.DUE_DATE} TEXT,
            ${Tasks.DUE_TIME} TEXT,
            ${Tasks.DUE_AT_MILLIS} INTEGER,
            ${Tasks.START_AT_MILLIS} INTEGER,
            ${Tasks.END_AT_MILLIS} INTEGER,
            ${Tasks.AWAITING_COMPLETION} INTEGER NOT NULL DEFAULT 0,
            ${Tasks.IS_OVERDUE} INTEGER NOT NULL DEFAULT 0,
            ${Tasks.OVERDUE_TEXT} TEXT,
            ${Tasks.ATTACHMENTS_COUNT} INTEGER,
            ${Tasks.NOTES} TEXT,
            ${Tasks.LOCATION} TEXT,
            ${Tasks.REMINDER} TEXT,
            ${Tasks.PROGRESS} INTEGER,
            ${Tasks.SPEC} TEXT,
            ${Tasks.COMPLETED_AT} TEXT,
            ${Tasks.IS_STARRED} INTEGER NOT NULL DEFAULT 0
        )
    """

    const val SQL_ADD_IS_STARRED =
        "ALTER TABLE ${Tasks.TABLE} ADD COLUMN ${Tasks.IS_STARRED} INTEGER NOT NULL DEFAULT 0"

    const val SQL_CREATE_SUBTASKS = """
        CREATE TABLE ${Subtasks.TABLE} (
            ${Subtasks.ID} TEXT,
            ${Subtasks.TASK_ID} TEXT,
            ${Subtasks.POSITION} INTEGER NOT NULL,
            ${Subtasks.TITLE} TEXT,
            ${Subtasks.COMPLETED} INTEGER NOT NULL DEFAULT 0,
            PRIMARY KEY (${Subtasks.TASK_ID}, ${Subtasks.POSITION}),
            FOREIGN KEY (${Subtasks.TASK_ID}) REFERENCES ${Tasks.TABLE}(${Tasks.ID}) ON DELETE CASCADE
        )
    """

    const val SQL_CREATE_IMAGES = """
        CREATE TABLE ${Images.TABLE} (
            ${Images.TASK_ID} TEXT,
            ${Images.POSITION} INTEGER NOT NULL,
            ${Images.URL} TEXT,
            ${Images.CAPTION} TEXT,
            PRIMARY KEY (${Images.TASK_ID}, ${Images.POSITION}),
            FOREIGN KEY (${Images.TASK_ID}) REFERENCES ${Tasks.TABLE}(${Tasks.ID}) ON DELETE CASCADE
        )
    """
}
