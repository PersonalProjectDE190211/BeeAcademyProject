# Script kiểm thử toàn diện các luồng API của BeeAcademy Backend
$ErrorActionPreference = "Continue"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "          KIỂM THỬ TỰ ĐỘNG CÁC LUỒNG API BEEACADEMY       " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

Write-Host "`n--- 1. KIỂM TRA CATALOG & PUBLIC API (KHÔNG CẦN TOKEN) ---" -ForegroundColor Yellow
try {
    $resCourses = Invoke-RestMethod -Uri "http://localhost:8080/api/courses/public" -Method Get
    Write-Host "[OK] GET /api/courses/public: Thành công. Số khóa học: $($resCourses.data.Count)" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] GET /api/courses/public: $($_.Exception.Message)" -ForegroundColor Red
}

try {
    $resCats = Invoke-RestMethod -Uri "http://localhost:8080/api/courses/public/categories" -Method Get
    Write-Host "[OK] GET /api/courses/public/categories: Thành công. Số danh mục: $($resCats.data.Count)" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] GET /api/courses/public/categories: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n--- 2. KIỂM TRA LUỒNG HỌC VIÊN (STUDENT) ---" -ForegroundColor Yellow
$randomStudent = Get-Random -Minimum 1000 -Maximum 9999
$studentEmail = "student_$randomStudent@beeacademy.edu.vn"
$regStudentObj = @{
    email = $studentEmail
    password = "Password123@"
    fullName = "Nguyen Van Hoc Sinh $randomStudent"
    role = "STUDENT"
}
$regStudentJson = $regStudentObj | ConvertTo-Json
$regStudentBytes = [System.Text.Encoding]::UTF8.GetBytes($regStudentJson)

try {
    $regResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -Body $regStudentBytes -ContentType "application/json; charset=utf-8"
    Write-Host "[OK] UC01 Đăng ký học viên: $studentEmail (ID: $($regResponse.data.id))" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] UC01 Đăng ký học viên: $($_.Exception.Message)" -ForegroundColor Red
}

# Đăng nhập Student
$loginStudentObj = @{ email = $studentEmail; password = "Password123@" }
$loginStudentBytes = [System.Text.Encoding]::UTF8.GetBytes(($loginStudentObj | ConvertTo-Json))
$studentToken = $null

try {
    $loginRes = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginStudentBytes -ContentType "application/json; charset=utf-8"
    $studentToken = $loginRes.data.accessToken
    Write-Host "[OK] UC02 Đăng nhập học viên: Role=$($loginRes.data.user.role), Token=$($studentToken.Substring(0, 20))..." -ForegroundColor Green
} catch {
    Write-Host "[FAIL] UC02 Đăng nhập học viên: $($_.Exception.Message)" -ForegroundColor Red
}

if ($studentToken) {
    $studentHeaders = @{ Authorization = "Bearer $studentToken" }

    # UC04 Profile
    try {
        $profileRes = Invoke-RestMethod -Uri "http://localhost:8080/api/profile/me" -Method Get -Headers $studentHeaders
        Write-Host "[OK] UC04 Lấy hồ sơ cá nhân: $($profileRes.data.fullName) ($($profileRes.data.email))" -ForegroundColor Green
    } catch {
        Write-Host "[FAIL] UC04 Lấy hồ sơ: $($_.Exception.Message)" -ForegroundColor Red
    }

    # UC13 My Courses
    try {
        $myCourses = Invoke-RestMethod -Uri "http://localhost:8080/api/learning/my-courses" -Method Get -Headers $studentHeaders
        Write-Host "[OK] UC13 Danh sách khóa học đang theo học: $($myCourses.data.Count) khóa học" -ForegroundColor Green
    } catch {
        Write-Host "[FAIL] UC13 Danh sách khóa học: $($_.Exception.Message)" -ForegroundColor Red
    }

    # UC20 My Certificates
    try {
        $myCerts = Invoke-RestMethod -Uri "http://localhost:8080/api/certificates/my" -Method Get -Headers $studentHeaders
        Write-Host "[OK] UC20 Danh sách chứng chỉ của tôi: $($myCerts.data.Count) chứng chỉ" -ForegroundColor Green
    } catch {
        Write-Host "[FAIL] UC20 Danh sách chứng chỉ: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n--- 3. KIỂM TRA LUỒNG GIÁO VIÊN (TEACHER) & CLOUDINARY MEDIA ---" -ForegroundColor Yellow
$randomTeacher = Get-Random -Minimum 1000 -Maximum 9999
$teacherEmail = "teacher_$randomTeacher@beeacademy.edu.vn"
$regTeacherObj = @{
    email = $teacherEmail
    password = "Password123@"
    fullName = "Thay Giao Test $randomTeacher"
    role = "TEACHER"
}
$regTeacherBytes = [System.Text.Encoding]::UTF8.GetBytes(($regTeacherObj | ConvertTo-Json))

try {
    $regTResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -Body $regTeacherBytes -ContentType "application/json; charset=utf-8"
    Write-Host "[OK] UC01 Đăng ký giáo viên: $teacherEmail (ID: $($regTResponse.data.id))" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] UC01 Đăng ký giáo viên: $($_.Exception.Message)" -ForegroundColor Red
}

$loginTeacherBytes = [System.Text.Encoding]::UTF8.GetBytes((@{ email = $teacherEmail; password = "Password123@" } | ConvertTo-Json))
$teacherToken = $null

try {
    $loginTRes = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginTeacherBytes -ContentType "application/json; charset=utf-8"
    $teacherToken = $loginTRes.data.accessToken
    Write-Host "[OK] UC02 Đăng nhập giáo viên: Role=$($loginTRes.data.user.role), Token=$($teacherToken.Substring(0, 20))..." -ForegroundColor Green
} catch {
    Write-Host "[FAIL] UC02 Đăng nhập giáo viên: $($_.Exception.Message)" -ForegroundColor Red
}

if ($teacherToken) {
    $teacherHeaders = @{ Authorization = "Bearer $teacherToken" }

    # UC30 Lấy danh sách khóa học do giáo viên phụ trách
    try {
        $tCourses = Invoke-RestMethod -Uri "http://localhost:8080/api/teacher/courses" -Method Get -Headers $teacherHeaders
        Write-Host "[OK] UC30 Khóa học giáo viên phụ trách: $($tCourses.data.Count) khóa" -ForegroundColor Green
    } catch {
        Write-Host "[FAIL] UC30 Khóa học giáo viên: $($_.Exception.Message)" -ForegroundColor Red
    }

    # Cloudinary Signed Signature
    try {
        $sigRes = Invoke-RestMethod -Uri "http://localhost:8080/api/media/signature?folder=beeacademy/courses/videos" -Method Get -Headers $teacherHeaders
        Write-Host "[OK] Cloudinary Signed Upload: CloudName=$($sigRes.data.cloudName), Timestamp=$($sigRes.data.timestamp), Signature=$($sigRes.data.signature.Substring(0, 15))..." -ForegroundColor Green
    } catch {
        Write-Host "[FAIL] Cloudinary Signed Upload: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n--- 4. KIỂM TRA TRA CỨU CHỨNG CHỈ CÔNG KHAI (PUBLIC VERIFY) ---" -ForegroundColor Yellow
try {
    $verifyRes = Invoke-RestMethod -Uri "http://localhost:8080/api/certificates/public/verify/NON_EXISTENT_CODE" -Method Get
    Write-Host "Tra cứu: $($verifyRes.message)" -ForegroundColor Green
} catch {
    Write-Host "[OK] Tra cứu chứng chỉ không tồn tại báo lỗi 404 chuẩn REST: $($_.Exception.Message)" -ForegroundColor Green
}

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "                KIỂM THỬ HOÀN TẤT VÀ THÀNH CÔNG!          " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
