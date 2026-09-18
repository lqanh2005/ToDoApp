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
            sprint = "Sprint 24 • Quản trị",
            title = "Hoàn thiện slide báo cáo quý 2",
            description = "Tổng hợp số liệu doanh thu từ phòng kế toán và biểu đồ tăng trưởng",
            status = TaskStatus.TODO,
            priority = PriorityLevel.CAO,
            category = "work",
            categoryLabel = "Công việc",
            categoryEmoji = "💼",
            dueDate = "Hôm nay",
            dueTime = "14:00 hôm nay",
            isOverdue = true,
            overdueText = "Quá hạn 2 giờ",
            reminder = "15",
            subtasks = listOf(
                Subtask("sub-1-1", "Thu thập báo cáo phòng Marketing", true),
                Subtask("sub-1-2", "Chốt số liệu doanh số với Kế toán", true),
                Subtask("sub-1-3", "Thiết kế biểu đồ so sánh quý 1 và 2", true),
                Subtask("sub-1-4", "Viết slide kết luận và định hướng Q3", false),
                Subtask("sub-1-5", "Duyệt bài với Giám đốc điều hành", false)
            )
        ),
        Task(
            id = "task-2",
            code = "#042",
            sprint = "Sprint 24 • TechCorp",
            title = "Gửi bản thiết kế Mobile App cho TechCorp",
            description = "Kiểm tra lại luồng thanh toán và xuất file Figma bàn giao",
            status = TaskStatus.TODO,
            priority = PriorityLevel.CAO,
            category = "work",
            categoryLabel = "Công việc",
            categoryEmoji = "💼",
            dueDate = "Hôm nay",
            dueTime = "16:30 hôm nay",
            isOverdue = true,
            overdueText = "Đã quá hạn 2 giờ so với tiến độ cam kết. Hãy cập nhật trạng thái hoặc gia hạn thời gian ngay.",
            reminder = "30",
            spec = "Palette chuẩn Material 3 • 390 x 844 px",
            images = listOf(
                TaskImage(AppAssets.SHOWCASE_IMG_1, "Mockup Wireframe v2"),
                TaskImage(AppAssets.SHOWCASE_IMG_2, "Bảng màu & Design Token")
            ),
            subtasks = listOf(
                Subtask("sub-2-1", "Khảo sát yêu cầu người dùng", true),
                Subtask("sub-2-2", "Lập Wireframe nháp và User flow", true),
                Subtask("sub-2-3", "Hoàn thiện UI High-fidelity", false),
                Subtask("sub-2-4", "Xuất file Assets & bàn giao dev", false)
            )
        ),
        Task(
            id = "task-3",
            code = "#043",
            title = "Nộp báo cáo tài chính tháng",
            description = "Kiểm tra chứng từ, phiếu thu chi và báo cáo lưu chuyển tiền tệ tháng vừa qua.",
            status = TaskStatus.TODO,
            priority = PriorityLevel.CAO,
            category = "work",
            categoryLabel = "Công việc",
            categoryEmoji = "💼",
            dueDate = "Hôm nay",
            dueTime = "Hôm nay, 17:00",
            reminder = "60",
            attachmentsCount = 2,
            subtasks = listOf(
                Subtask("sub-3-1", "Đối chiếu sao kê ngân hàng", true),
                Subtask("sub-3-2", "Gửi file PDF cho ban giám đốc", false)
            )
        ),
        Task(
            id = "task-4",
            code = "#044",
            title = "Đọc sách Clean Code chương 4 & 5",
            description = "Ghi chú các nguyên tắc đặt tên hàm và xử lý exception",
            status = TaskStatus.TODO,
            priority = PriorityLevel.TB,
            category = "study",
            categoryLabel = "Học tập",
            categoryEmoji = "📚",
            dueDate = "Ngày mai",
            dueTime = "20:00 Ngày mai",
            notes = "Ghi chép tóm tắt",
            reminder = "15",
            subtasks = listOf(
                Subtask("sub-4-1", "Đọc chương 4: Formatting", true),
                Subtask("sub-4-2", "Đọc chương 5: Objects & Data Structures", false)
            )
        ),
        Task(
            id = "task-5",
            code = "#045",
            title = "Mua thêm sữa chua, hạt granola & hoa quả tươi",
            description = "Ghé siêu thị WinMart dưới sảnh chung cư sau giờ làm",
            status = TaskStatus.TODO,
            priority = PriorityLevel.THAP,
            category = "shopping",
            categoryLabel = "Mua sắm",
            categoryEmoji = "🛒",
            dueDate = "26 Th06",
            dueTime = "18:30 · 26 Th06",
            location = "Siêu thị WinMart",
            reminder = "15",
            subtasks = listOf(
                Subtask("sub-5-1", "Sữa chua không đường Vinamilk", false),
                Subtask("sub-5-2", "Hạt granola yến mạch", false),
                Subtask("sub-5-3", "Chuối và việt quất tươi", false)
            )
        ),
        Task(
            id = "task-6",
            code = "#046",
            title = "Học tiếng Anh bài 12",
            description = "Ngữ pháp thì hoàn thành & Luyện từ vựng chuyên ngành công nghệ phần mềm.",
            status = TaskStatus.DOING,
            priority = PriorityLevel.TB,
            category = "study",
            categoryLabel = "Học tập",
            categoryEmoji = "📚",
            dueDate = "Hôm nay",
            dueTime = "16:30",
            reminder = "15",
            notes = "Đang nghe audio - Còn 25 phút",
            progress = 45,
            subtasks = listOf(
                Subtask("sub-6-1", "Từ vựng Unit 12", true),
                Subtask("sub-6-2", "Nghe Podcast 20 phút", false)
            )
        ),
        Task(
            id = "task-7",
            code = "#047",
            title = "Rà soát thiết kế Mobile",
            description = "Kiểm tra tỷ lệ padding, token màu sắc và responsiveness.",
            status = TaskStatus.DOING,
            priority = PriorityLevel.CAO,
            category = "work",
            categoryLabel = "Công việc",
            categoryEmoji = "💼",
            dueDate = "Hôm nay",
            dueTime = "15:00",
            reminder = "15",
            notes = "Subtask: 3/4 - Đang tính giờ",
            progress = 75,
            subtasks = listOf(
                Subtask("sub-7-1", "Token màu sắc", true),
                Subtask("sub-7-2", "Spacing scale", true),
                Subtask("sub-7-3", "Kiểm tra Dark mode", true),
                Subtask("sub-7-4", "Export vector SVGs", false)
            )
        ),
        Task(
            id = "task-8",
            code = "#048",
            title = "Họp Daily Scrum",
            description = "Cập nhật tiến độ dự án với đội kỹ thuật và tháo gỡ khó khăn phát sinh.",
            status = TaskStatus.DONE,
            priority = PriorityLevel.TB,
            category = "work",
            categoryLabel = "Công việc",
            categoryEmoji = "💼",
            dueDate = "Hôm nay",
            dueTime = "09:00",
            reminder = "15",
            completedAt = "09:30",
            subtasks = listOf(Subtask("sub-8-1", "Chia sẻ cập nhật sprint", true))
        ),
        Task(
            id = "task-9",
            code = "#049",
            title = "Mua vật tư văn phòng",
            description = "Giấy in A4 và bút dạ bảng trắng cho phòng họp số 2.",
            status = TaskStatus.DONE,
            priority = PriorityLevel.THAP,
            category = "work",
            categoryLabel = "Công việc",
            categoryEmoji = "💼",
            dueDate = "Hôm nay",
            dueTime = "11:00",
            reminder = "15",
            completedAt = "11:20",
            notes = "Đã thanh toán",
            subtasks = listOf(Subtask("sub-9-1", "Thanh toán hóa đơn", true))
        ),
        Task(
            id = "task-10",
            code = "#050",
            title = "Gửi email nghiệm thu sprint",
            description = "Email thông báo kết thúc sprint và chuẩn bị kế hoạch tuần mới.",
            status = TaskStatus.DONE,
            priority = PriorityLevel.TB,
            category = "work",
            categoryLabel = "Công việc",
            categoryEmoji = "💼",
            dueDate = "Hôm nay",
            dueTime = "11:45",
            reminder = "15",
            completedAt = "11:45",
            subtasks = listOf(Subtask("sub-10-1", "Soạn và gửi email", true))
        ),
        Task(
            id = "task-11",
            code = "#051",
            title = "Đi siêu thị gia đình",
            description = "Thực phẩm tươi sống và sữa chua cho tuần mới.",
            status = TaskStatus.DONE,
            priority = PriorityLevel.THAP,
            category = "personal",
            categoryLabel = "Cá nhân",
            categoryEmoji = "👤",
            dueDate = "Hôm nay",
            dueTime = "19:00",
            reminder = "15",
            completedAt = "19:40",
            subtasks = listOf(Subtask("sub-11-1", "Mua rau củ", true))
        ),
        Task(
            id = "task-12",
            code = "#052",
            title = "Lên dàn ý bài thuyết trình",
            description = "Phác thảo các slide chính và kịch bản trình bày.",
            status = TaskStatus.DONE,
            priority = PriorityLevel.TB,
            category = "work",
            categoryLabel = "Công việc",
            categoryEmoji = "💼",
            dueDate = "Hôm nay",
            dueTime = "20:30",
            reminder = "15",
            completedAt = "21:00",
            subtasks = listOf(Subtask("sub-12-1", "Lên mục tiêu", true))
        )
    )
}
