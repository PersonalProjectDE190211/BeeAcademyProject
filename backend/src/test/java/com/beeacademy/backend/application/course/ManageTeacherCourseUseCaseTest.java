package com.beeacademy.backend.application.course;

import com.beeacademy.backend.application.course.dto.AddChapterCommand;
import com.beeacademy.backend.application.course.dto.AddLessonCommand;
import com.beeacademy.backend.application.course.dto.CourseDetailResult;
import com.beeacademy.backend.application.course.dto.CreateCourseCommand;
import com.beeacademy.backend.application.course.usecase.ManageTeacherCourseUseCase;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import com.beeacademy.backend.domain.course.CourseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("ManageTeacherCourseUseCase Unit Tests (UC30, UC31)")
class ManageTeacherCourseUseCaseTest {

    private CourseRepositoryPort courseRepository;
    private ManageTeacherCourseUseCase useCase;

    @BeforeEach
    void setUp() {
        courseRepository = Mockito.mock(CourseRepositoryPort.class);
        useCase = new ManageTeacherCourseUseCase(courseRepository);
    }

    @Test
    @DisplayName("Giáo viên tạo khóa học mới ở trạng thái DRAFT")
    void createCourse_Success() {
        UUID teacherId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        CreateCourseCommand cmd = new CreateCourseCommand(
                teacherId, categoryId, "Hóa Học Lớp 10", "Cơ bản đến nâng cao",
                "https://cdn.example.com/thumb.jpg", 120000, List.of(10)
        );

        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CourseDetailResult result = useCase.createCourse(cmd);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Hóa Học Lớp 10");
        assertThat(result.status()).isEqualTo(CourseStatus.DRAFT.name());
        assertThat(result.priceVnd()).isEqualTo(120000);
        assertThat(result.totalChapters()).isZero();
        assertThat(result.totalLessons()).isZero();
    }

    @Test
    @DisplayName("Giáo viên thêm chương và bài giảng -> tự động tính lại tổng chương, bài và thời lượng")
    void addChapterAndLesson_Success() {
        UUID teacherId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Course course = Course.create(teacherId, categoryId, "Vật Lý 11", "Điện tích", null, null, List.of(11));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 1. Thêm chương
        AddChapterCommand addChapterCmd = new AddChapterCommand(course.getId(), teacherId, "Chương 1: Điện tích", "Giới thiệu");
        CourseDetailResult afterChapter = useCase.addChapter(addChapterCmd);
        assertThat(afterChapter.totalChapters()).isEqualTo(1);
        UUID chapterId = afterChapter.chapters().get(0).id();

        // 2. Thêm bài học
        AddLessonCommand addLessonCmd = new AddLessonCommand(
                course.getId(), teacherId, chapterId, "Bài 1: Định luật Coulomb",
                "https://video.example.com/coulomb.mp4", 900, true
        );
        CourseDetailResult afterLesson = useCase.addLesson(addLessonCmd);
        assertThat(afterLesson.totalLessons()).isEqualTo(1);
        assertThat(afterLesson.totalDurationSec()).isEqualTo(900);
        assertThat(afterLesson.chapters().get(0).lessons().get(0).videoUrl()).isEqualTo("https://video.example.com/coulomb.mp4");
    }

    @Test
    @DisplayName("Giáo viên khác không thể can thiệp vào khóa học không thuộc quyền sở hữu")
    void modifyCourse_ForbiddenForDifferentTeacher() {
        UUID ownerId = UUID.randomUUID();
        UUID hackerId = UUID.randomUUID();
        Course course = Course.create(ownerId, UUID.randomUUID(), "Toán 12", "Giải tích", null, null, List.of(12));

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        AddChapterCommand cmd = new AddChapterCommand(course.getId(), hackerId, "Chương hack", "Hacking");
        assertThatThrownBy(() -> useCase.addChapter(cmd))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Bạn không có quyền chỉnh sửa khóa học của giáo viên khác");
    }
}

