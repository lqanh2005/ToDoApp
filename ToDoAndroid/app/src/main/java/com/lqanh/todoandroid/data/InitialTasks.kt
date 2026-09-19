package com.lqanh.todoandroid.data

object AppAssets {
    const val PROFILE_AVATAR =
        "https://lh3.googleusercontent.com/aida-public/AB6AXuAxhltLl3G6AlZUvq79RruuPIYeo-EYxEY9Cr3RbErBQimnXFhSQByL-deHVk0P4Is4ELe6FC9K6QUmY5Bf60PteQm4VKS1dZqOg0hrad5uMLe88-8g2g0f9Ehl9VSPU_CYIoanJoP-zsm-HhU9mfwwixcK-SY8F2KIE7YgAqCUTr2BIbEyKVhEXeODKY_RGHHV1E3-nUItjsyYUyYpevM-78ZiFk0OgpnmyXTxqdv0JRSvKzK2KGyDRw"

    const val SHOWCASE_IMG_1 =
        "https://lh3.googleusercontent.com/aida-public/AB6AXuDv3tvdYNHowpft0kDy5miQqYulktX3HFZS_Cmt6DI7QiCA0FXXhWNxnsRM5Ww7R65Wli7-Br2lc49uoIgDfOWl-7b4ngnaY-9HEop0Wzdpzc6muJwFB047XmjvhD4_YsErHbRUJZDz5rdUnzuymj9TKDcg4pkhOhHcv0eVO1Sf0dTWOG0xY6k7sa4pFvB7ptmCe5kyNhgSV31ACWwQ7Bv07_zNIV_YnTHtdDkQ5WjXMQ13yTR0M5ITVg"

    const val SHOWCASE_IMG_2 =
        "https://lh3.googleusercontent.com/aida-public/AB6AXuD6mbVvzbxCBTO6qm5QFM6Krh3ZaQi4lUpf7Y2fVC7Qi2_3UU8yfr-i7_Bogb0QgDE7mzU8HG5gT1tjhTMpp00O8AfATam-oklPXICI3vfJlX2x6GMReVmd-3rv-dYgfkkKPgD1JnWQzrq5yDp1lXE8ABfDh6OwIhGa6MCgvS3qw6e3rJm0D62sEI2vOE-oGjN13CkCeAIIEJpj7LPHdMdvK7C-9Bx6tJm7q0kTE1PbsxjwXX4AWPIq5A"
}

object InitialTasks {
    val list: List<Task> = listOf(
        Task(
            id = "task-1",
            code = "#041",
            sprint = "Sprint 24 • Admin",
            title = "Finish Q2 report slides",
            description = "Compile revenue figures from accounting and growth charts",
            status = TaskStatus.TODO,
            priority = PriorityLevel.CAO,
            category = "work",
            categoryLabel = "Work",
            categoryEmoji = "💼",
            dueDate = "Today",
            dueTime = "14:00 today",
            isOverdue = true,
            overdueText = "2 hours overdue",
            reminder = "15",
            subtasks = listOf(
                Subtask("sub-1-1", "Collect Marketing department report", true),
                Subtask("sub-1-2", "Confirm sales figures with Accounting", true),
                Subtask("sub-1-3", "Design Q1 vs Q2 comparison charts", true),
                Subtask("sub-1-4", "Write conclusion slides and Q3 outlook", false),
                Subtask("sub-1-5", "Review with the CEO", false)
            )
        ),
        Task(
            id = "task-2",
            code = "#042",
            sprint = "Sprint 24 • TechCorp",
            title = "Send Mobile App design to TechCorp",
            description = "Review payment flow and export Figma handoff files",
            status = TaskStatus.TODO,
            priority = PriorityLevel.CAO,
            category = "work",
            categoryLabel = "Work",
            categoryEmoji = "💼",
            dueDate = "Today",
            dueTime = "16:30 today",
            isOverdue = true,
            overdueText = "2 hours overdue vs the committed timeline. Update status or extend the deadline.",
            reminder = "30",
            spec = "Material 3 palette • 390 x 844 px",
            images = listOf(
                TaskImage(AppAssets.SHOWCASE_IMG_1, "Mockup Wireframe v2"),
                TaskImage(AppAssets.SHOWCASE_IMG_2, "Color board & Design Tokens")
            ),
            subtasks = listOf(
                Subtask("sub-2-1", "Survey user requirements", true),
                Subtask("sub-2-2", "Draft wireframes and user flow", true),
                Subtask("sub-2-3", "Finish high-fidelity UI", false),
                Subtask("sub-2-4", "Export assets & hand off to dev", false)
            )
        ),
        Task(
            id = "task-3",
            code = "#043",
            title = "Submit monthly financial report",
            description = "Review receipts, vouchers, and cash-flow for the past month.",
            status = TaskStatus.TODO,
            priority = PriorityLevel.CAO,
            category = "work",
            categoryLabel = "Work",
            categoryEmoji = "💼",
            dueDate = "Today",
            dueTime = "Today, 17:00",
            reminder = "60",
            attachmentsCount = 2,
            subtasks = listOf(
                Subtask("sub-3-1", "Reconcile bank statements", true),
                Subtask("sub-3-2", "Send PDF to the board", false)
            )
        ),
        Task(
            id = "task-4",
            code = "#044",
            title = "Read Clean Code chapters 4 & 5",
            description = "Note naming principles and exception handling",
            status = TaskStatus.TODO,
            priority = PriorityLevel.TB,
            category = "study",
            categoryLabel = "Study",
            categoryEmoji = "📚",
            dueDate = "Tomorrow",
            dueTime = "20:00 Tomorrow",
            notes = "Summary notes",
            reminder = "15",
            subtasks = listOf(
                Subtask("sub-4-1", "Read chapter 4: Formatting", true),
                Subtask("sub-4-2", "Read chapter 5: Objects & Data Structures", false)
            )
        ),
        Task(
            id = "task-5",
            code = "#045",
            title = "Buy yogurt, granola & fresh fruit",
            description = "Stop by WinMart in the lobby after work",
            status = TaskStatus.TODO,
            priority = PriorityLevel.THAP,
            category = "shopping",
            categoryLabel = "Shopping",
            categoryEmoji = "🛒",
            dueDate = "Jun 26",
            dueTime = "18:30 · Jun 26",
            location = "WinMart",
            reminder = "15",
            subtasks = listOf(
                Subtask("sub-5-1", "Unsweetened Vinamilk yogurt", false),
                Subtask("sub-5-2", "Oat granola", false),
                Subtask("sub-5-3", "Bananas and fresh blueberries", false)
            )
        ),
        Task(
            id = "task-6",
            code = "#046",
            title = "English lesson 12",
            description = "Perfect tense grammar & software engineering vocabulary.",
            status = TaskStatus.DOING,
            priority = PriorityLevel.TB,
            category = "study",
            categoryLabel = "Study",
            categoryEmoji = "📚",
            dueDate = "Today",
            dueTime = "16:30",
            reminder = "15",
            notes = "Listening to audio - 25 minutes left",
            progress = 45,
            subtasks = listOf(
                Subtask("sub-6-1", "Unit 12 vocabulary", true),
                Subtask("sub-6-2", "Listen to a 20-minute podcast", false)
            )
        ),
        Task(
            id = "task-7",
            code = "#047",
            title = "Review mobile design",
            description = "Check padding ratios, color tokens, and responsiveness.",
            status = TaskStatus.DOING,
            priority = PriorityLevel.CAO,
            category = "work",
            categoryLabel = "Work",
            categoryEmoji = "💼",
            dueDate = "Today",
            dueTime = "15:00",
            reminder = "15",
            notes = "Subtask: 3/4 - Timer running",
            progress = 75,
            subtasks = listOf(
                Subtask("sub-7-1", "Color tokens", true),
                Subtask("sub-7-2", "Spacing scale", true),
                Subtask("sub-7-3", "Dark mode check", true),
                Subtask("sub-7-4", "Export vector SVGs", false)
            )
        ),
        Task(
            id = "task-8",
            code = "#048",
            title = "Daily Scrum meeting",
            description = "Update project progress with the eng team and unblock issues.",
            status = TaskStatus.DONE,
            priority = PriorityLevel.TB,
            category = "work",
            categoryLabel = "Work",
            categoryEmoji = "💼",
            dueDate = "Today",
            dueTime = "09:00",
            reminder = "15",
            completedAt = "09:30",
            subtasks = listOf(Subtask("sub-8-1", "Share sprint update", true))
        ),
        Task(
            id = "task-9",
            code = "#049",
            title = "Buy office supplies",
            description = "A4 paper and whiteboard markers for meeting room 2.",
            status = TaskStatus.DONE,
            priority = PriorityLevel.THAP,
            category = "work",
            categoryLabel = "Work",
            categoryEmoji = "💼",
            dueDate = "Today",
            dueTime = "11:00",
            reminder = "15",
            completedAt = "11:20",
            notes = "Paid",
            subtasks = listOf(Subtask("sub-9-1", "Pay the invoice", true))
        ),
        Task(
            id = "task-10",
            code = "#050",
            title = "Send sprint acceptance email",
            description = "Announce sprint completion and prepare next week's plan.",
            status = TaskStatus.DONE,
            priority = PriorityLevel.TB,
            category = "work",
            categoryLabel = "Work",
            categoryEmoji = "💼",
            dueDate = "Today",
            dueTime = "11:45",
            reminder = "15",
            completedAt = "11:45",
            subtasks = listOf(Subtask("sub-10-1", "Draft and send email", true))
        ),
        Task(
            id = "task-11",
            code = "#051",
            title = "Family grocery run",
            description = "Fresh produce and yogurt for the new week.",
            status = TaskStatus.DONE,
            priority = PriorityLevel.THAP,
            category = "personal",
            categoryLabel = "Personal",
            categoryEmoji = "👤",
            dueDate = "Today",
            dueTime = "19:00",
            reminder = "15",
            completedAt = "19:40",
            subtasks = listOf(Subtask("sub-11-1", "Buy vegetables", true))
        ),
        Task(
            id = "task-12",
            code = "#052",
            title = "Outline presentation",
            description = "Draft key slides and speaking script.",
            status = TaskStatus.DONE,
            priority = PriorityLevel.TB,
            category = "work",
            categoryLabel = "Work",
            categoryEmoji = "💼",
            dueDate = "Today",
            dueTime = "20:30",
            reminder = "15",
            completedAt = "21:00",
            subtasks = listOf(Subtask("sub-12-1", "Set goals", true))
        )
    )
}
