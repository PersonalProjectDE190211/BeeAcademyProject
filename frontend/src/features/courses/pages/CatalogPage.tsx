import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  ArrowRight,
  BookOpen,
  Clock,
  Filter,
  GraduationCap,
  Search,
  Sparkles,
  Star,
} from 'lucide-react';
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

const POPULAR_GRADES = [
  { label: 'Tất cả lớp', value: '' },
  { label: 'Lớp 6', value: '6' },
  { label: 'Lớp 7', value: '7' },
  { label: 'Lớp 8', value: '8' },
  { label: 'Lớp 9', value: '9' },
  { label: 'Lớp 10', value: '10' },
  { label: 'Lớp 11', value: '11' },
  { label: 'Lớp 12', value: '12' },
];

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
    <div className="space-y-7">
      {/* Warm Terracotta Branded Hero Section */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-brand-700 via-brand-500 to-brand-300 p-6 sm:p-10 text-white shadow-xl">
        {/* Subtle Decorative Hexagons */}
        <svg
          className="absolute -top-10 -right-10 opacity-15 pointer-events-none"
          width="240"
          height="240"
          viewBox="0 0 100 100"
          aria-hidden="true"
        >
          <polygon points="50,3 93,27 93,73 50,97 7,73 7,27" fill="#fff" />
        </svg>
        <svg
          className="absolute -bottom-12 right-1/4 opacity-10 pointer-events-none"
          width="180"
          height="180"
          viewBox="0 0 100 100"
          aria-hidden="true"
        >
          <polygon points="50,3 93,27 93,73 50,97 7,73 7,27" fill="#fff" />
        </svg>

        <div className="relative z-10 max-w-3xl space-y-4">
          <div className="inline-flex items-center gap-2 rounded-full bg-white/20 px-3.5 py-1 text-xs font-semibold backdrop-blur-md">
            <Sparkles className="h-3.5 w-3.5 text-butter" />
            <span>Chương trình Giáo dục Phổ thông K–12 Toàn diện</span>
          </div>

          <h1 className="text-2xl sm:text-4xl font-extrabold tracking-tight leading-tight">
            Khám phá Kho Khóa học BeeAcademy
          </h1>
          <p className="text-sm sm:text-base text-cream/90 font-normal leading-relaxed max-w-2xl">
            Video bài giảng sinh động, ngân hàng câu hỏi công thức Toán/Lý/Hóa LaTeX chấm điểm tức thì, và theo dõi tiến độ cá nhân hóa.
          </p>

          {/* Search Bar */}
          <form
            onSubmit={handleSearchSubmit}
            className="pt-2 flex flex-col sm:flex-row gap-2.5 max-w-xl"
          >
            <div className="relative flex-1">
              <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-ink-muted" />
              <input
                type="text"
                placeholder="Tìm kiếm môn học, chuyên đề, bài giảng..."
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                className="w-full h-11 pl-10 pr-4 rounded-xl border-0 bg-white text-ink text-sm shadow-md focus:ring-2 focus:ring-butter focus:outline-none placeholder:text-stone-400"
              />
            </div>
            <Button
              type="submit"
              size="md"
              className="bg-ink hover:bg-stone-800 text-white font-semibold shadow-md shrink-0"
            >
              Tìm kiếm
            </Button>
          </form>
        </div>
      </div>

      {/* Grade Quick Filter Chips */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1 scrollbar-none">
        <span className="text-xs font-bold text-ink shrink-0 mr-1 flex items-center gap-1.5">
          <GraduationCap className="h-4 w-4 text-brand-700" />
          Khối lớp:
        </span>
        {POPULAR_GRADES.map((g) => {
          const isSelected = grade === g.value;
          return (
            <button
              key={g.value}
              type="button"
              onClick={() => setGrade(g.value)}
              className={`rounded-xl px-3.5 py-1.5 text-xs font-semibold transition-all cursor-pointer whitespace-nowrap ${
                isSelected
                  ? 'bg-gradient-to-r from-brand-700 to-brand-500 text-white shadow-xs scale-105'
                  : 'bg-white border border-sand text-ink-muted hover:border-sand-strong hover:bg-cream hover:text-ink'
              }`}
            >
              {g.label}
            </button>
          );
        })}
      </div>

      {/* Filter & Sort Bar */}
      <Card className="p-4 border-sand bg-white shadow-2xs">
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
            label="Khối lớp (Lớp 1 - 12)"
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

      {/* Course Cards Grid */}
      {loading ? (
        <Spinner label="Đang tải danh sách khóa học BeeAcademy..." />
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
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {courses.map((course) => (
            <Card
              key={course.id}
              className="group flex flex-col justify-between overflow-hidden p-0 border-sand bg-white shadow-2xs hover:shadow-xl hover:-translate-y-1 transition-all duration-200 rounded-2xl"
            >
              <div>
                {/* Thumbnail */}
                <div className="relative h-48 w-full bg-sand-soft overflow-hidden">
                  {course.thumbnailUrl ? (
                    <img
                      src={course.thumbnailUrl}
                      alt={course.title}
                      className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
                    />
                  ) : (
                    <div className="flex h-full items-center justify-center bg-gradient-to-br from-cream to-blush text-brand-700">
                      <BookOpen className="h-14 w-14 opacity-40 group-hover:scale-110 transition-transform" />
                    </div>
                  )}

                  {/* Price Tag Pill */}
                  <div className="absolute top-3 right-3">
                    <span
                      className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-extrabold shadow-sm ${
                        course.priceVnd === 0
                          ? 'bg-emerald-600 text-white'
                          : 'bg-white/95 text-brand-700 backdrop-blur-sm border border-sand'
                      }`}
                    >
                      {course.priceVnd === 0 ? 'Miễn phí' : formatVnd(course.priceVnd)}
                    </span>
                  </div>
                </div>

                {/* Content */}
                <div className="p-5">
                  <h3 className="line-clamp-2 text-base font-bold text-ink group-hover:text-brand-700 transition-colors">
                    {course.title}
                  </h3>
                  <p className="mt-2 line-clamp-2 text-xs text-ink-muted leading-relaxed">
                    {course.description}
                  </p>

                  <div className="mt-4 flex flex-wrap items-center gap-3 text-xs text-ink-muted pt-2 border-t border-sand-soft">
                    <span className="inline-flex items-center gap-1 font-medium">
                      <BookOpen className="h-3.5 w-3.5 text-brand-700" />
                      {course.totalChapters} chương • {course.totalLessons} bài
                    </span>
                    <span className="inline-flex items-center gap-1">
                      <Clock className="h-3.5 w-3.5 text-stone-400" />
                      {Math.max(1, Math.round(course.totalDurationSec / 60))} phút
                    </span>
                    <span className="inline-flex items-center gap-1 font-bold text-amber-600 ml-auto">
                      <Star className="h-3.5 w-3.5 fill-amber-400 text-amber-400" />
                      {Number(course.averageRating || 0).toFixed(1)} ({course.totalReviews})
                    </span>
                  </div>
                </div>
              </div>

              {/* Action */}
              <div className="border-t border-sand-soft bg-cream/50 px-5 py-3.5">
                <Link to={`/courses/${course.id}`} className="block">
                  <Button
                    variant="primary"
                    className="w-full text-xs font-bold shadow-xs flex items-center justify-center gap-2 group-hover:shadow-md transition-shadow"
                    size="sm"
                  >
                    <span>Xem chi tiết & Học thử</span>
                    <ArrowRight className="h-3.5 w-3.5 transition-transform group-hover:translate-x-1" />
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
