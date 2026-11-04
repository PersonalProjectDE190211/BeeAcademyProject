# Legacy SQL Migrations Archive

Thư mục này tạm thời lưu trữ 10 script SQL cũ (Supabase migrations) để đối chiếu, tham khảo các cấu trúc bảng (schema, columns, constraints, RLS) trong quá trình hoàn thiện các file Flyway migrations chính thức tại `backend/src/main/resources/db/migration/`.

### Danh sách file lưu trữ:
1. `supabase_migration_admin_view.sql`
2. `supabase_migration_bank.sql`
3. `supabase_migration_complaints.sql`
4. `supabase_migration_enrollments.sql`
5. `supabase_migration_payment.sql`
6. `supabase_migration_payment_full.sql`
7. `supabase_migration_payos.sql`
8. `supabase_migration_qa.sql`
9. `supabase_migration_revenue.sql`
10. `supabase_migration_teacher_quiz.sql`

*Lưu ý: Sau khi hoàn thành kiểm thử và triển khai 8 file Flyway migration V1 -> V8, thư mục này có thể xóa bỏ hoàn toàn.*

