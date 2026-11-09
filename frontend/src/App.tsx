import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AppShell, RequireAuth } from '@/app/layout/AppShell';
import { LoginPage } from '@/features/auth/pages/LoginPage';
import { RegisterPage } from '@/features/auth/pages/RegisterPage';
import { ForgotPasswordPage } from '@/features/auth/pages/ForgotPasswordPage';
import { ProfileSettingsPage } from '@/features/auth/pages/ProfileSettingsPage';
import { CatalogPage } from '@/features/courses/pages/CatalogPage';
import { CourseDetailPage } from '@/features/courses/pages/CourseDetailPage';
import { TeacherCoursesPage } from '@/features/courses/pages/TeacherCoursesPage';
import { AdminCoursesApprovalPage } from '@/features/courses/pages/AdminCoursesApprovalPage';
import { StudentLearningPage } from '@/features/learning/pages/StudentLearningPage';
import { CourseLearningRoomPage } from '@/features/learning/pages/CourseLearningRoomPage';
import { CertificatesPage } from '@/features/learning/pages/CertificatesPage';
import { TeacherQuestionBankPage } from '@/features/assessment/pages/TeacherQuestionBankPage';
import { StudentQuizRoomPage } from '@/features/assessment/pages/StudentQuizRoomPage';
import { StudentOrdersPage } from '@/features/commerce/pages/StudentOrdersPage';
import { TeacherRevenuePage } from '@/features/commerce/pages/TeacherRevenuePage';
import { AdminPayoutsPage } from '@/features/commerce/pages/AdminPayoutsPage';
import { ParentPortalPage } from '@/features/family/pages/ParentPortalPage';
import { StudentFamilyPage } from '@/features/family/pages/StudentFamilyPage';
import { ComplaintsPage } from '@/features/support/pages/ComplaintsPage';
import { NotificationsPage } from '@/features/support/pages/NotificationsPage';
import { AdminDashboardPage } from '@/features/admin/pages/AdminDashboardPage';
import { AdminUsersPage } from '@/features/admin/pages/AdminUsersPage';
import { AdminSettingsPage } from '@/features/admin/pages/AdminSettingsPage';

export default function App() {
  return (
    <BrowserRouter>
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 3500,
          style: {
            borderRadius: '12px',
            fontSize: '13px',
            fontWeight: 500,
          },
        }}
      />
      <Routes>
        {/* Dedicated Auth Routes (UC01, UC02) - Full-screen Split Layout */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />

        <Route element={<AppShell />}>
          <Route index element={<Navigate to="/courses" replace />} />

          {/* Public Course Catalog (UC06, UC07, UC08) */}
          <Route path="/courses" element={<CatalogPage />} />
          <Route path="/courses/:courseId" element={<CourseDetailPage />} />

          {/* Authenticated User Profile & Support Routes */}
          <Route
            path="/profile"
            element={
              <RequireAuth>
                <ProfileSettingsPage />
              </RequireAuth>
            }
          />
          <Route
            path="/support/complaints"
            element={
              <RequireAuth>
                <ComplaintsPage />
              </RequireAuth>
            }
          />
          <Route
            path="/notifications"
            element={
              <RequireAuth>
                <NotificationsPage />
              </RequireAuth>
            }
          />

          {/* Student Routes (UC09 - UC21, UC24, UC29) */}
          <Route
            path="/student/learning"
            element={
              <RequireAuth allowedRoles={['student', 'admin']}>
                <StudentLearningPage />
              </RequireAuth>
            }
          />
          <Route
            path="/learning/my-courses"
            element={
              <RequireAuth allowedRoles={['student', 'admin']}>
                <StudentLearningPage />
              </RequireAuth>
            }
          />
          <Route
            path="/learning/courses/:courseId"
            element={
              <RequireAuth>
                <CourseLearningRoomPage />
              </RequireAuth>
            }
          />
          <Route
            path="/learning/chapters/:chapterId/quiz"
            element={
              <RequireAuth>
                <StudentQuizRoomPage />
              </RequireAuth>
            }
          />
          <Route
            path="/student/certificates"
            element={
              <RequireAuth>
                <CertificatesPage />
              </RequireAuth>
            }
          />
          <Route
            path="/learning/certificates"
            element={
              <RequireAuth>
                <CertificatesPage />
              </RequireAuth>
            }
          />
          <Route
            path="/student/orders"
            element={
              <RequireAuth allowedRoles={['student']}>
                <StudentOrdersPage />
              </RequireAuth>
            }
          />
          <Route
            path="/orders"
            element={
              <RequireAuth allowedRoles={['student']}>
                <StudentOrdersPage />
              </RequireAuth>
            }
          />
          <Route
            path="/student/family"
            element={
              <RequireAuth allowedRoles={['student']}>
                <StudentFamilyPage />
              </RequireAuth>
            }
          />

          {/* Parent Portal Routes (UC24 - UC29) */}
          <Route
            path="/parent/portal"
            element={
              <RequireAuth allowedRoles={['parent']}>
                <ParentPortalPage />
              </RequireAuth>
            }
          />

          {/* Teacher Studio Routes (UC30 - UC37) */}
          <Route
            path="/teacher/courses"
            element={
              <RequireAuth allowedRoles={['teacher', 'admin']}>
                <TeacherCoursesPage />
              </RequireAuth>
            }
          />
          <Route
            path="/teacher/questions"
            element={
              <RequireAuth allowedRoles={['teacher', 'admin']}>
                <TeacherQuestionBankPage />
              </RequireAuth>
            }
          />
          <Route
            path="/teacher/revenue"
            element={
              <RequireAuth allowedRoles={['teacher']}>
                <TeacherRevenuePage />
              </RequireAuth>
            }
          />

          {/* Admin Governance Routes (UC38 - UC45) */}
          <Route
            path="/admin/dashboard"
            element={
              <RequireAuth allowedRoles={['admin']}>
                <AdminDashboardPage />
              </RequireAuth>
            }
          />
          <Route
            path="/admin/courses"
            element={
              <RequireAuth allowedRoles={['admin']}>
                <AdminCoursesApprovalPage />
              </RequireAuth>
            }
          />
          <Route
            path="/admin/users"
            element={
              <RequireAuth allowedRoles={['admin']}>
                <AdminUsersPage />
              </RequireAuth>
            }
          />
          <Route
            path="/admin/payouts"
            element={
              <RequireAuth allowedRoles={['admin']}>
                <AdminPayoutsPage />
              </RequireAuth>
            }
          />
          <Route
            path="/admin/settings"
            element={
              <RequireAuth allowedRoles={['admin']}>
                <AdminSettingsPage />
              </RequireAuth>
            }
          />

          <Route path="*" element={<Navigate to="/courses" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
