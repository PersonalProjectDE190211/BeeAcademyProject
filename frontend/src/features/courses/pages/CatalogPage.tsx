import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { BookOpen, Clock, Filter, Search, Star } from 'lucide-react';
import { CategoryItem, courseApi, CourseSummary } from '../api/courseApi';
import { formatVnd, getApiErrorMessage } from '@/shared/types/api';
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Input,
  Select,
  Spinner,
} from '@/shared/ui/primitives';

export const CatalogPage: React.FC = () => {
  const [courses, setCourses] = useState<CourseSummary[]>([]);
  const [categories, setCategories] = useState<CategoryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [keyword, setKeyword] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [grade, setGrade] = useState('');
  const [sortBy, setSortBy] = useState('newest');

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [courseList, catList] = await Promise.all([
        courseApi.searchPublicCourses({
          keyword: keyword.trim() || undefined,
          categoryId: categoryId || undefined,
          grade: grade ? Number(grade) : undefined,
          sortBy,
        }),
        courseApi.getCategories().catch(() => []),
      ]);
      setCourses(courseList);
      setCategories(catList);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Không thể tải danh mục khóa học.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [categoryId, grade, sortBy]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchData();
  };

  return (
    <div className="space-y-6">
      <div className="rounded-2xl bg-gradient-to-r from-amber-500 to-amber-600 p-6 text-white shadow-sm sm:p-8">
        <h1 className="text-2xl font-bold sm:text-3xl">
          Khám phá Kho Khóa học Phổ thông K-12 BeeAcademy
        </h1>
        <p className="mt-2 max-w-2xl text-sm text-amber-50">
          Hệ thống bài giảng chuẩn chương trình Phổ thông từ Lớp 1 đến Lớp 12, kết hợp Ngân hàng câu hỏi công thức Toán/Lý/Hóa LaTeX và Chấm điểm tức thì.
        </p>

        <form onSubmit={handleSearchSubmit} className="mt-5 flex flex-col gap-3 sm:flex-row">
          <div className="flex-1">
            <Input
              placeholder="Tìm khóa học theo tên môn, chuyên đề..."
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              className="border-white/20 bg-white text-slate-900"
            />
          </div>
          <Button type="submit" variant="secondary" size="lg">
            <Search className="h-4 w-4" />
            Tìm kiếm
          </Button>
        </form>
      </div>

      <Card className="p-4">
        <div className="grid gap-3 sm:grid-cols-3">
          <Select
            label="Môn học / Chuyên mục"
            value={categoryId}
            onChange={(e) => setCategoryId(e.target.value)}
          >
            <option value="">Tất cả môn học</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </Select>

          <Select
            label="Khối lớp (Lớp 1 - Lớp 12)"
            value={grade}
            onChange={(e) => setGrade(e.target.value)}
          >
            <option value="">Tất cả khối lớp</option>
            {Array.from({ length: 12 }, (_, idx) => idx + 1).map((g) => (
              <option key={g} value={String(g)}>
                Khối lớp {g}
              </option>
            ))}
          </Select>

          <Select
            label="Sắp xếp theo"
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
          >
            <option value="newest">Mới xuất bản nhất</option>
            <option value="price_asc">Học phí: Thấp đến Cao</option>
            <option value="price_desc">Học phí: Cao đến Thấp</option>
            <option value="rating">Đánh giá cao nhất</option>
          </Select>
        </div>
      </Card>

      {loading ? (
        <Spinner label="Đang tải danh sách khóa học..." />
      ) : error ? (
        <ErrorState message={error} onRetry={fetchData} />
      ) : courses.length === 0 ? (
        <EmptyState
          title="Không tìm thấy khóa học phù hợp"
          description="Hãy thử thay đổi bộ lọc khối lớp, môn học hoặc từ khóa tìm kiếm."
          action={
            <Button
              variant="outline"
              onClick={() => {
                setKeyword('');
                setCategoryId('');
                setGrade('');
                setSortBy('newest');
              }}
            >
              <Filter className="h-4 w-4" />
              Xóa bộ lọc
            </Button>
          }
        />
      ) : (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {courses.map((course) => (
            <Card key={course.id} className="flex flex-col justify-between overflow-hidden p-0">
              <div>
                <div className="relative h-44 w-full bg-slate-100">
                  {course.thumbnailUrl ? (
                    <img
                      src={course.thumbnailUrl}
                      alt={course.title}
                      className="h-full w-full object-cover"
                    />
                  ) : (
                    <div className="flex h-full items-center justify-center bg-amber-50 text-amber-600">
                      <BookOpen className="h-12 w-12 opacity-60" />
                    </div>
                  )}
                  <div className="absolute top-3 right-3">
                    <Badge tone={course.priceVnd === 0 ? 'success' : 'brand'}>
                      {course.priceVnd === 0 ? 'Miễn phí' : formatVnd(course.priceVnd)}
                    </Badge>
                  </div>
                </div>

                <div className="p-5">
                  <h3 className="line-clamp-2 text-base font-bold text-slate-900">
                    {course.title}
                  </h3>
                  <p className="mt-1.5 line-clamp-2 text-xs text-slate-600">
                    {course.description}
                  </p>

                  <div className="mt-4 flex flex-wrap items-center gap-3 text-xs text-slate-500">
                    <span className="inline-flex items-center gap-1">
                      <BookOpen className="h-3.5 w-3.5 text-amber-600" />
                      {course.totalChapters} chương • {course.totalLessons} bài
                    </span>
                    <span className="inline-flex items-center gap-1">
                      <Clock className="h-3.5 w-3.5 text-slate-400" />
                      {Math.max(1, Math.round(course.totalDurationSec / 60))} phút
                    </span>
                    <span className="inline-flex items-center gap-1 font-semibold text-amber-600">
                      <Star className="h-3.5 w-3.5 fill-amber-400 text-amber-400" />
                      {Number(course.averageRating || 0).toFixed(1)} ({course.totalReviews})
                    </span>
                  </div>
                </div>
              </div>

              <div className="border-t border-slate-100 bg-slate-50/60 px-5 py-3">
                <Link to={`/courses/${course.id}`} className="block">
                  <Button variant="primary" className="w-full" size="sm">
                    Xem chi tiết & Học thử
                  </Button>
                </Link>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};
