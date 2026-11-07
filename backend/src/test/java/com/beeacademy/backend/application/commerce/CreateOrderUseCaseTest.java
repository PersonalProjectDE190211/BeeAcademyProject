package com.beeacademy.backend.application.commerce;

import com.beeacademy.backend.application.commerce.dto.CreateOrderCommand;
import com.beeacademy.backend.application.commerce.dto.CreateOrderResult;
import com.beeacademy.backend.application.commerce.usecase.CreateOrderUseCase;
import com.beeacademy.backend.domain.commerce.Order;
import com.beeacademy.backend.domain.commerce.OrderRepositoryPort;
import com.beeacademy.backend.domain.commerce.RewardVoucherRepositoryPort;
import com.beeacademy.backend.domain.common.CourseSlug;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.MoneyVnd;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import com.beeacademy.backend.domain.course.CourseStatus;
import com.beeacademy.backend.domain.learning.EnrollmentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private CourseRepositoryPort courseRepository;

    @Mock
    private EnrollmentRepositoryPort enrollmentRepository;

    @Mock
    private RewardVoucherRepositoryPort voucherRepository;

    private CreateOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateOrderUseCase(orderRepository, courseRepository, enrollmentRepository, voucherRepository);
    }

    private Course createTestCourse(UUID id, UUID teacherId, String title, MoneyVnd price, CourseStatus status) {
        return new Course(
                id,
                teacherId,
                UUID.randomUUID(),
                title,
                CourseSlug.fromTitle(title),
                "Mô tả khóa học",
                "https://example.com/thumb.png",
                price,
                status,
                List.of(10),
                1,
                5,
                3600,
                BigDecimal.valueOf(5.0),
                10,
                Instant.now(),
                Instant.now(),
                Instant.now(),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    @Test
    @DisplayName("Tạo đơn hàng thành công khi khóa học đã xuất bản và học sinh chưa ghi danh")
    void createOrder_Success() {
        UUID studentId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        Course course = createTestCourse(courseId, teacherId, "Luyện thi Toán vào 10", MoneyVnd.of(300000), CourseStatus.PUBLISHED);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderCommand command = new CreateOrderCommand(studentId, List.of(courseId), null, "PAYOS");
        CreateOrderResult result = useCase.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.totalAmountVnd()).isEqualTo(300000);
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.paymentRef()).isNotBlank();
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).courseTitle()).isEqualTo("Luyện thi Toán vào 10");

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Ném lỗi khi học sinh đã ghi danh khóa học từ trước")
    void createOrder_AlreadyEnrolled_ThrowsException() {
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        Course course = createTestCourse(courseId, UUID.randomUUID(), "Khóa học nâng cao", MoneyVnd.of(200000), CourseStatus.PUBLISHED);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(true);

        CreateOrderCommand command = new CreateOrderCommand(studentId, List.of(courseId), null, "PAYOS");

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Học sinh đã đăng ký khóa học");
    }
}
