# 📊 Báo Cáo Tiến Độ Dự Án & Định Hướng Phát Triển — Bee Academy

Tài liệu này ghi lại chi tiết trạng thái tiến độ các chức năng (Use Case theo đặc tả `UC.md`), ma trận ánh xạ Frontend/Backend, các thành phần kỹ thuật đã hoàn thành và lộ trình phát triển tiếp theo của hệ thống **BeeAcademy**.

---

## 📈 1. Bảng Tổng Hợp Chỉ Số Tiến Độ (Executive Dashboard)

| Chỉ số | Số lượng / Tỷ lệ | Trạng thái | Ghi chú |
| :--- | :---: | :---: | :--- |
| **Tổng số Use Cases (`UC.md`)** | **44 Use Cases** | **44/44 (100%)** | 8 Module nghiệp vụ cốt lõi |
| **Frontend Pages & Routes** | **18 Màn hình & Trợ lý AI** | **100% Hoàn thành** | Chuẩn thiết kế *Warm Terracotta Motion* |
| **Backend Clean Architecture** | **16 Controllers / 9 Domains** | **100% Hoàn thành** | Java 21, Spring Boot 3.3, DDD |
| **Cơ sở dữ liệu (Supabase/Postgres)** | **20 Bảng & Migration Flyway** | **100% Hoàn thành** | Double-Entry Ledger, Audit Logs |
| **Tích hợp Cổng thanh toán VietQR** | **PayOS / Napas 24/7** | **100% Hoàn thành** | Sinh mã QR động, Polling & đếm ngược 15p |
| **Kiểm tra công thức Toán/Lý/Hóa** | **KaTeX (`LatexRenderer`)** | **100% Hoàn thành** | Ngân hàng câu hỏi & Phòng thi trắc nghiệm |
| **Dữ liệu Giáo trình & Tài liệu thật** | **6 Khóa học / 35 File PDF** | **100% Hoàn thành** | 13 chương, 28 bài giảng có video YouTube & PDF đính kèm |
| **Trợ lý AI (Gemini Flash / BeeBot)** | **1 Use Case cốt lõi (UC22)** | **100% Hoàn thành** | Trợ lý Tư vấn Tuyển sinh & Khóa học K-12 thông minh cho Phụ huynh & Học sinh |

---

## 🎨 2. Hệ Thống Thiết Kế Giao Diện (Design System: Warm Terracotta Motion)

Toàn bộ giao diện người dùng (Frontend) được chuẩn hóa theo ngôn ngữ thiết kế **Warm Terracotta Motion**:
* **Bảng màu chủ đạo:**
  * **Brand Primary:** Terracotta `#b02e00` (đậm), `#d4501a` (chuẩn), `#e8743a` (sáng).
  * **Nền & Bề mặt:** Cream `#fffaf5`, Blush `#fff4ec`, Peach `#ffd9c2`, Butter `#ffe9b8`.
  * **Đường viền:** Sand `#e7ddd5`, Sand strong `#d9b9a4`.
  * **Văn bản:** Ink `#1c1917`, Ink-muted `#57534e`.
* **Typography:** `Be Vietnam Pro` (400, 500, 600, 700) tối ưu tiếng Việt và hiển thị đồng bộ công thức Toán/Lý/Hóa KaTeX.
* **Thành phần Trình phát Video đa năng (`VideoPlayer`):** Tự động phát hiện và render iframe YouTube chuẩn tỉ lệ 16:9 hoặc thẻ HTML5 `<video>` cho Cloudinary/MP4; tích hợp thanh kiểm soát tua Anti-Skip (yêu cầu xem $\ge 80\%$ để hoàn thành).
* **Modal Thanh toán VietQR Napas 24/7 (`VietQrPaymentModal`):** Sinh mã QR động theo từng đơn hàng, nút sao chép thông tin tài khoản 1-click, đồng hồ đếm ngược 15 phút và cơ chế polling tự động mở khóa bài học.

---

## 📋 3. Bảng Theo Dõi Tiến Độ Chi Tiết Theo 8 Module (`UC.md`)

---

### 🔑 Module 1 — Xác Thực & Tài Khoản (5 UC — Hoàn thành 100%)
*Toàn bộ module đã kết nối API thật, bảo mật JWT kép (Access + Refresh Token), Google OAuth2 và Cloudinary Avatar.*

| Mã UC | Tên Use Case | Actor | Trạng thái | Thành phần Frontend | API Backend |
| :---: | :--- | :--- | :---: | :--- | :--- |
| **UC01** | Đăng ký tài khoản | Guest | ✅ 100% | `RegisterPage.tsx` | `POST /api/auth/register`, `POST /api/auth/verify-otp` |
| **UC02** | Đăng nhập hệ thống | Học sinh, PH, GV, Admin | ✅ 100% | `LoginPage.tsx`, `GoogleSignInButton.tsx` | `POST /api/auth/login`, `POST /api/auth/google` |
| **UC03** | Đăng xuất hệ thống | Học sinh, PH, GV, Admin | ✅ 100% | `AppShell.tsx` | `POST /api/auth/logout` |
| **UC04** | Đặt lại mật khẩu | Guest | ✅ 100% | `ForgotPasswordPage.tsx` | `POST /api/auth/forgot-password`, `POST /api/auth/reset-password` |
| **UC05** | Cập nhật hồ sơ cá nhân | Học sinh, PH, GV | ✅ 100% | `ProfileSettingsPage.tsx` | `GET /api/profile/me`, `PUT /api/profile/me`, `POST /api/media/upload` (Bicubic Downsampling) |

---

### 🔍 Module 2 — Tìm Kiếm & Khóa Học (3 UC — Hoàn thành 100%)
*Kho bài giảng K-12 kết nối cơ sở dữ liệu thật, lọc nhanh theo khối lớp và xem bài học thử qua VideoPlayer.*

| Mã UC | Tên Use Case | Actor | Trạng thái | Thành phần Frontend | API Backend |
| :---: | :--- | :--- | :---: | :--- | :--- |
| **UC06** | Tìm kiếm khóa học | Guest, Học sinh | ✅ 100% | `CatalogPage.tsx` (Chips Lớp 6-12, Search) | `GET /api/courses/public` |
| **UC07** | Xem chi tiết khóa học | Guest, Học sinh | ✅ 100% | `CourseDetailPage.tsx` (Đề cương, Học phí, Review) | `GET /api/courses/public/{id}`, `GET /api/courses/{id}/reviews` |
| **UC08** | Xem bài học thử | Guest, Học sinh | ✅ 100% | `CourseDetailPage.tsx` (`VideoPlayer` modal khi `isFreePreview=true`) | `GET /api/courses/public/{id}` |

---

### 💳 Module 3 — Mua Hàng & Thanh Toán (4 UC — Hoàn thành 100%)
*Luồng thanh toán tiền về tài khoản công ty, ghi nhận Double-Entry Ledger và phân chia doanh thu tự động.*

| Mã UC | Tên Use Case | Actor | Trạng thái | Thành phần Frontend | API Backend |
| :---: | :--- | :--- | :---: | :--- | :--- |
| **UC09** | Mua khóa học | Học sinh, PH | ✅ 100% | `CourseDetailPage.tsx` (Nút Mua khóa học mở ngay modal) | `POST /api/student/orders` |
| **UC10** | Thanh toán VietQR | Học sinh, PH | ✅ 100% | `VietQrPaymentModal.tsx` (Mã QR PayOS Napas 24/7, đếm ngược 15p) | `POST /api/student/orders`, `POST /api/webhooks/payos` |
| **UC11** | Xem lịch sử mua khóa học | Học sinh, PH | ✅ 100% | `StudentOrdersPage.tsx` (Xem trạng thái, mở lại QR, hủy đơn) | `GET /api/student/orders`, `POST /api/student/orders/{id}/cancel` |
| **UC12** | Gửi khiếu nại đến Admin | Học sinh, PH | ✅ 100% | `ComplaintsPage.tsx` (Tạo phiếu hỗ trợ/khiếu nại học phí) | `POST /api/support/complaints` |

---

### 📖 Module 4 — Học Tập & Đánh Giá (8 UC — Hoàn thành 100%)
*Phòng học trực tuyến chống tua (Anti-Skip), ngân hàng câu hỏi công thức Toán/Lý/Hóa LaTeX và tự động cấp chứng chỉ số.*

| Mã UC | Tên Use Case | Actor | Trạng thái | Thành phần Frontend | API Backend |
| :---: | :--- | :--- | :---: | :--- | :--- |
| **UC13** | Xem danh sách khóa học đã mua | Học sinh | ✅ 100% | `StudentLearningPage.tsx` (Thanh % tiến độ) | `GET /api/learning/my-courses` |
| **UC14** | Xem bài giảng & tài liệu | Học sinh | ✅ 100% | `CourseLearningRoomPage.tsx` (`VideoPlayer` YouTube/MP4) | `GET /api/learning/courses/{cId}/lessons/{lId}` |
| **UC15** | Tải tài liệu học tập | Học sinh | ✅ 100% | `CourseLearningRoomPage.tsx` (Tải tài liệu đính kèm) | `GET /api/learning/courses/{cId}/lessons/{lId}` |
| **UC16** | Nộp bài tập & Trao đổi | Học sinh | ✅ 100% | `CourseLearningRoomPage.tsx` (Q&A dưới bài giảng) | `POST /api/learning/courses/{cId}/lessons/{lId}/discussions` |
| **UC17** | Làm bài kiểm tra (Quiz) | Học sinh | ✅ 100% | `StudentQuizRoomPage.tsx` (Sinh đề thi ngẫu nhiên) | `POST /api/learning/chapters/{id}/quiz/start` |
| **UC18** | Xem điểm & Tiến độ học tập | Học sinh | ✅ 100% | `StudentQuizRoomPage.tsx` (Chấm điểm tức thì, giải thích KaTeX) | `POST /api/learning/quiz/submit`, `POST /api/learning/track-progress` |
| **UC19** | Đánh giá khóa học | Học sinh | ✅ 100% | `CourseDetailPage.tsx` (Form chấm 1-5 sao + bình luận) | `POST /api/courses/{id}/reviews` |
| **UC20** | Xem & tải chứng chỉ số | Học sinh | ✅ 100% | `CertificatesPage.tsx` (Xuất PDF A4 Landscape, tra cứu QR) | `GET /api/learning/certificates/me`, `GET /api/certificates/{id}/download` |

---

### 💬 Module 5 — Tương Tác & Hỗ Trợ (3 UC — Hoàn thành 100%)
*Trợ lý học tập AI BeeBot giải đáp 24/7 theo phương pháp Socratic, hiển thị công thức KaTeX và cố vấn lộ trình cá nhân hóa.*

| Mã UC | Tên Use Case | Actor | Trạng thái | Thành phần Frontend | API Backend |
| :---: | :--- | :--- | :---: | :--- | :--- |
| **UC21** | Gửi câu hỏi cho giáo viên | Học sinh | ✅ 100% | `CourseLearningRoomPage.tsx` (Tab thảo luận bài học) | `POST /api/learning/discussions/{id}/replies` |
| **UC22** | Trợ lý AI tư vấn tuyển sinh & khóa học | Phụ huynh, Học sinh | ✅ 100% | `BeeBotAssistant.tsx` (Floating AI Drawer, tư vấn khóa học 6-12, học phí) | `POST /api/ai/chat` (Gemini 3.5 Flash-Lite + Catalog) |

---

### 👨‍👩‍👧 Module 6 — Phân Hệ Phụ Huynh & Gia Đình (6 UC — Hoàn thành 100%)
*Cổng theo dõi tiến độ dành riêng cho phụ huynh với cơ chế đồng thuận mã mời 6 ký tự và nhật ký Privacy Audit Log.*

| Mã UC | Tên Use Case | Actor | Trạng thái | Thành phần Frontend | API Backend |
| :---: | :--- | :--- | :---: | :--- | :--- |
| **UC24** | Theo dõi tiến độ học của con | Phụ huynh | ✅ 100% | `ParentPortalPage.tsx` (Xem biểu đồ % bài học, điểm số) | `GET /api/family/parent/students/{id}/progress` |
| **UC25** | Liên hệ & nhận thông báo từ GV | Phụ huynh | ✅ 100% | `NotificationsPage.tsx`, `ComplaintsPage.tsx` | `GET /api/support/notifications` |
| **UC26** | Xem lịch sử thanh toán khóa học | Phụ huynh | ✅ 100% | `ParentPortalPage.tsx` | `GET /api/family/parent/links` |
| **UC27** | Gửi lời mời liên kết con | Phụ huynh | ✅ 100% | `ParentPortalPage.tsx` (Nhập email con $\rightarrow$ sinh mã 6 ký tự) | `POST /api/family/parent/invite` |
| **UC28** | Chấp nhận / từ chối liên kết | Học sinh | ✅ 100% | `StudentFamilyPage.tsx` (Nhập mã 6 ký tự để Consent) | `POST /api/family/student/accept-invite`, `reject-invite` |
| **UC29** | Hủy liên kết tài khoản | Học sinh, PH | ✅ 100% | Cả hai trang `ParentPortalPage.tsx` & `StudentFamilyPage.tsx` | `POST /api/family/parent/links/{id}/unlink`, `student/unlink` |

---

### 👨‍🏫 Module 7 — Phân Hệ Giáo Viên (8 UC — Hoàn thành 100%)
*Studio soạn giảng, ngân hàng đề thi công thức toán học LaTeX và cơ chế chia sẻ doanh thu 80/20.*

| Mã UC | Tên Use Case | Actor | Trạng thái | Thành phần Frontend | API Backend |
| :---: | :--- | :--- | :---: | :--- | :--- |
| **UC30** | Tạo khóa học mới | Giáo viên | ✅ 100% | `TeacherCoursesPage.tsx` (Tạo khóa, gửi Admin duyệt) | `POST /api/teacher/courses` |
| **UC31** | Cập nhật bài giảng & tài liệu | Giáo viên | ✅ 100% | `TeacherCoursesPage.tsx` (Thêm chương, thêm video bài học) | `POST /api/teacher/courses/{id}/chapters`, `/lessons` |
| **UC32** | Tạo question bank | Giáo viên | ✅ 100% | `TeacherQuestionBankPage.tsx` (Nhập câu hỏi kèm KaTeX) | `POST /api/teacher/questions` |
| **UC33** | Cập nhật question bank | Giáo viên | ✅ 100% | `TeacherQuestionBankPage.tsx` (Xóa, sửa câu hỏi trắc nghiệm) | `DELETE /api/teacher/questions/{id}` |
| **UC34** | Cấu hình ma trận đề kiểm tra | Giáo viên | ✅ 100% | `TeacherQuestionBankPage.tsx` (Số câu Dễ/Vừa/Khó, thời gian) | `POST /api/teacher/chapters/{id}/quiz-config` |
| **UC35** | Chấm điểm bài tập & Quiz | Giáo viên | ✅ 100% | Hệ thống chấm tự động theo ma trận câu hỏi | `POST /api/learning/quiz/submit` |
| **UC36** | Trả lời câu hỏi học sinh | Giáo viên | ✅ 100% | `CourseLearningRoomPage.tsx` (Trả lời thắc mắc của học sinh) | `POST /api/learning/discussions/{id}/replies` |
| **UC37** | Xem lịch sử doanh thu & STK | Giáo viên | ✅ 100% | `TeacherRevenuePage.tsx` (Xem 80% thực nhận, nhập STK AES-256) | `GET /api/teacher/finance/statistics`, `POST /api/teacher/finance/bank-account` |

---

### 👑 Module 8 — Quản Trị Viên (7 UC — Hoàn thành 100%)
*Trung tâm điều hành GMV sàn, duyệt khóa học, đối soát chi trả tiền cho giáo viên và quản trị an toàn thông tin.*

| Mã UC | Tên Use Case | Actor | Trạng thái | Thành phần Frontend | API Backend |
| :---: | :--- | :--- | :---: | :--- | :--- |
| **UC38** | Xem dashboard quản trị | Admin | ✅ 100% | `AdminDashboardPage.tsx` (GMV, quỹ giữ hộ, phí sàn 20%) | `GET /api/admin/dashboard` |
| **UC39** | Xem danh sách người dùng | Admin | ✅ 100% | `AdminUsersPage.tsx` (Danh sách HS, GV, Phụ huynh) | `GET /api/admin/users` |
| **UC40** | Mở và khóa tài khoản | Admin | ✅ 100% | `AdminUsersPage.tsx` (Khóa/Mở tài khoản, duyệt hồ sơ GV) | `POST /api/admin/users/{id}/lock`, `unlock`, `provision-teacher` |
| **UC41** | Duyệt khóa học xuất bản | Admin | ✅ 100% | `AdminCoursesApprovalPage.tsx` (Phê duyệt / Từ chối khóa học) | `POST /api/admin/courses/{id}/decision` |
| **UC42** | Xử lý khiếu nại từ người dùng | Admin | ✅ 100% | `ComplaintsPage.tsx` (Trao đổi tin nhắn, đánh dấu đã giải quyết) | `POST /api/admin/complaints/{id}/messages`, `resolve` |
| **UC43** | Xác nhận chuyển khoản GV | Admin | ✅ 100% | `AdminPayoutsPage.tsx` (Thẩm định STK, nhập mã UNC quyết toán) | `POST /api/admin/payouts/{id}/confirm`, `bank-accounts/{id}/verify` |
| **UC44** | Cấu hình & Bật/Tắt bảo trì | Admin | ✅ 100% | `AdminSettingsPage.tsx` (Bật Maintenance mode, xem Audit logs) | `GET/POST /api/admin/settings/maintenance` |

---

## 📚 4. Dữ Liệu Giáo Trình Khóa Học & Tài Liệu Thật Đính Kèm (Real Curriculum & Document Assets)

Hệ thống đã được nạp dữ liệu hoàn chỉnh cho **toàn bộ 6 khóa học** với **13 chương**, **28 bài học video streaming** và **35 tài liệu học tập thật (PDF/Tài liệu tham khảo chuyên sâu)**:

| Khóa học | ID Khóa học | Khối Lớp | Giáo viên | Số chương / bài | Tài liệu PDF đính kèm tiêu biểu |
| :--- | :--- | :---: | :--- | :---: | :--- |
| **Toán 6 — Khởi Đầu Vững Chắc** | `619f46b8-96bb-43ba-801c-a6ef913f6c54` | Lớp 6 | Thầy Hoàng Minh Đức | 3 chương / 6 bài | • Sơ đồ tư duy Tập hợp Toán 6<br>• Phiếu bài tập rèn luyện Tập hợp có đáp án<br>• Chuyên đề tính nhanh & thứ tự phép tính<br>• Bộ đề trắc nghiệm dấu hiệu chia hết<br>• Sàng Eratosthenes & bài tập số nguyên tố<br>• Thực hành cắt dán hình học trực quan |
| **Toán 12 — Chinh Phục Điểm 9+** | `67d48484-7fed-40f2-9f1d-2f576f8a6ca6` | Lớp 12 | Thầy Hoàng Minh Đức | 2 chương / 6 bài | • Sổ tay công thức Đạo hàm & Bảng biến thiên 12<br>• 100 câu trắc nghiệm Cực trị hàm số có đáp án<br>• Chuyên đề Min Max hàm hợp & Casio 880BTG<br>• Bảng 60 công thức Nguyên hàm giải nhanh ĐH<br>• Kỹ thuật sơ đồ đường chéo tích phân từng phần<br>• Chuyên đề bài toán diện tích thể tích thực tế |
| **Vật Lý 12 — Dao Động & Sóng** | `6cf4854f-ae2e-4210-846f-58cedbebbddc` | Lớp 12 | Cô Phan Thanh Thảo | 2 chương / 4 bài | • Sổ tay công thức Dao động điều hòa & Trục thời gian<br>• Bài tập pha ban đầu và năng lượng dao động<br>• Chuyên đề đồ thị lực đàn hồi con lắc lò xo<br>• Tóm tắt lý thuyết sự truyền sóng cơ & độ lệch pha<br>• Bài tập cực đại cực tiểu giao thoa sóng 9+ |
| **Hóa Học 12 — Este & Hợp Chất Nitơ** | `d35d21ba-4b7e-4a50-b877-44cd21d0eaa5` | Lớp 12 | Thầy Nguyễn Tiến Thành | 2 chương / 4 bài | • Sơ đồ phản ứng thủy phân este và đồng phân este<br>• 50 câu trắc nghiệm Este Lipit lý thuyết trọng tâm<br>• Kỹ thuật dồn chất và đồng đẳng hóa chất béo 9+<br>• Bảng so sánh tính bazơ của Amin & quy đổi<br>• Chuyên đề Peptit nâng cao vận dụng cao THPTQG |
| **Toán 9 — Luyện Thi Vào Lớp 10** | `de892ca3-ae06-4d8e-9a8f-adec4313684d` | Lớp 9 | Thầy Hoàng Minh Đức | 2 chương / 4 bài | • Sổ tay phương pháp rút gọn phân thức chứa căn 9<br>• 20 đề thi thử vào 10 phần rút gọn biểu thức<br>• Chuyên đề bài toán phụ sau rút gọn thi vào 10<br>• Kỹ năng giải và biện luận hệ phương trình chứa m<br>• Bộ 30 bài hình học tứ giác nội tiếp điểm 9+ |
| **Tiếng Anh 10 — Global Success** | `46b1e9a4-1688-4314-96aa-611921a85741` | Lớp 10 | Cô Phan Thanh Thảo | 2 chương / 4 bài | • Comprehensive English Grammar Chart - All 12 Tenses<br>• Practice Exercises - Tenses with Keys<br>• Passive Voice Rules & Sentence Transformation<br>• Academic Vocabulary List 500 Words Global Success<br>• Skimming and Scanning Techniques for Students |

> [!NOTE]
> **Khắc phục lỗi HTTP 500 khi tra cứu chi tiết khóa học:**
> Đã nâng cấp `SpringDataCourseRepository.java` chuyển từ đa phép `LEFT JOIN FETCH` (gây ngoại lệ `MultipleBagFetchException` trong Hibernate) sang cơ chế `@EntityGraph(attributePaths = {"chapters"})` kết hợp `@BatchSize(size = 50)` trên các tập hợp `chapters` và `lessons`. Tích hợp hoàn chỉnh việc serialize/deserialize mảng tài liệu JSONB `documents` giữa cơ sở dữ liệu Postgres và giao diện học viên.

---

## 🔑 5. Danh Sách Tài Khoản Thử Nghiệm & Kịch Bản Kiểm Thử (Testing Credentials)

Tất cả tài khoản thử nghiệm sử dụng chung mật khẩu bảo mật: **`Bee@123456`**

| Vai trò | Email đăng nhập | Mật khẩu | Họ và tên | Kịch bản kiểm thử đề xuất |
| :--- | :--- | :---: | :--- | :--- |
| **Quản trị viên (Admin)** | `admin@beeacademy.edu.vn` | `Bee@123456` | Quản Trị Viên BeeAcademy | • Xem Dashboard GMV & quỹ giữ hộ (`/admin/dashboard`)<br>• Phê duyệt / từ chối khóa học (`/admin/courses`)<br>• Quản lý khóa / mở tài khoản (`/admin/users`)<br>• Thẩm định STK ngân hàng & xác nhận UNC chi trả GV (`/admin/payouts`)<br>• Bật/tắt chế độ bảo trì (`/admin/settings`) |
| **Giáo viên (Teacher 1)** | `thayduc.toan@beeacademy.edu.vn` | `Bee@123456` | Thầy Hoàng Minh Đức | • Soạn giảng khóa học Toán 12, Toán 9, Toán 6 (`/teacher/courses`)<br>• Soạn câu hỏi trắc nghiệm KaTeX LaTeX (`/teacher/questions`)<br>• Xem thống kê doanh thu 80% thực nhận & tài khoản MBBank (`/teacher/revenue`) |
| **Giáo viên (Teacher 2)** | `cophan.vatly@beeacademy.edu.vn` | `Bee@123456` | Cô Phan Thanh Thảo | • Quản lý khóa học Vật Lý 12 & Tiếng Anh 10 (`/teacher/courses`)<br>• Đóng góp câu hỏi ngân hàng đề thi trắc nghiệm |
| **Học sinh (Student 1)** | `hocsinh.nam@beeacademy.edu.vn` | `Bee@123456` | Nguyễn Văn Nam (Lớp 12) | • Vào phòng học Toán 12, xem video có kiểm soát Anti-Skip $\ge 80\%$ (`/learning/courses/...`)<br>• Làm bài kiểm tra Quiz trắc nghiệm Toán 12 (`/learning/chapters/.../quiz`)<br>• Xem & tải Chứng chỉ số hoàn thành khóa học Toán 6 (`/student/certificates`)<br>• Xem đơn hàng & test quét mã VietQR (`/student/orders`)<br>• Quản lý quyền chia sẻ tiến độ với Phụ huynh (`/student/family`) |
| **Học sinh (Student 2)** | `hocsinh.mai@beeacademy.edu.vn` | `Bee@123456` | Lê Ngọc Mai (Lớp 9) | • Nhập mã mời `BEE123` tại `/student/family` để test luồng Chấp thuận / Từ chối liên kết Phụ huynh |
| **Phụ huynh (Parent)** | `phuhuynh.tuan@beeacademy.edu.vn` | `Bee@123456` | Bác Nguyễn Minh Tuấn | • Truy cập Cổng Phụ huynh (`/parent/portal`) theo dõi tiến độ của em Nam<br>• Gửi mã mời liên kết mới cho con qua email |

---

## 🎯 6. Kế Hoạch & Định Hướng Phát Triển Tiếp Theo

1. **Hoàn thiện Module Trợ lý AI (Gemini 2.5 Flash):**
   * Kết nối `gemini.api-key` ở Backend Spring Boot để kích hoạt **UC22 (Chat AI giải bài tập)** và **UC23 (Đề xuất lộ trình)**.
   * Xây dựng widget chat AI nổi (Floating AI Assistant) trong phòng học `CourseLearningRoomPage.tsx`.
2. **Cấu hình Webhook PayOS cho môi trường Production:**
   * Cung cấp URL webhook công khai trên máy chủ Render/Vercel để PayOS tự động gửi tín hiệu xác nhận thanh toán tức thời mà không cần chờ học sinh bấm kiểm tra.
3. **Mở rộng Ngân hàng Đề thi Đánh giá Năng lực (Exam Matrix):**
   * Bổ sung bộ đề thi tổng hợp thi thử vào lớp 10 và thi tốt nghiệp THPT Quốc Gia với đồng hồ đếm ngược 50 - 90 phút.
